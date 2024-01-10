/*
 * Copyright 2024 C Thing Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cthing.locc4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.io.input.CharSequenceReader;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.lang.System.Logger.Level.TRACE;

import static org.cthing.locc4j.Counter.ParsingMode.CODE;
import static org.cthing.locc4j.Counter.ParsingMode.COMMENT;
import static org.cthing.locc4j.Counter.ParsingMode.STRING;
import static org.cthing.locc4j.Counter.QuoteType.DOC;
import static org.cthing.locc4j.Counter.QuoteType.NORMAL;
import static org.cthing.locc4j.Counter.QuoteType.VERBATIM;


/**
 * Counts lines in the specified character data according to the specified language.
 */
@NotThreadSafe
class Counter {

    /**
     * Counter parsing modes.
     */
    enum ParsingMode {
        /** Parsing outside a string and comment. Can transition to {@link #STRING} or {@link #COMMENT} mode. */
        CODE,
        /** Parsing within a string. Can transition to {@link #CODE}. */
        STRING,
        /** Parsing within a comment. Can transition to {@link #CODE}. */
        COMMENT,
    }

    /**
     * Indicates the type of quote encountered while parsing.
     */
    enum QuoteType {
        /** Indicates that quotations are being interpreted normally (e.g. double quote). */
        NORMAL,
        /** Indicates that quotations represent a documentation string. */
        DOC,
        /** Indicates that quotations represents character data that require no escaping. */
        VERBATIM,
    }

    private static final System.Logger LOGGER = System.getLogger(Counter.class.getName());
    private static final JsonPointer JUPYTER_LANGUAGE_PTR = JsonPointer.compile("/metadata/kernelspec/language");
    private static final JsonPointer JUPYTER_EXTENSION_PTR = JsonPointer.compile("/metadata/language_info/file_extension");

    private final Language language;
    private final Config config;
    @Nullable
    private CharSequence quote;
    private QuoteType quoteType;
    private final LinkedList<CharSequence> commentStack;

    Counter(final Language language, final Config config) {
        this.language = language;
        this.config = config;
        this.quoteType = NORMAL;
        this.commentStack = new LinkedList<>();
    }

    /**
     * Performs the counting of lines in the data specified in the constructor. Stats will be added to the specified
     * stats object. Note that the counts are added to any existing counts in the specified stats object.
     *
     * @param characters Text to be counted
     * @param fileStats Object to collect the line count stats.
     * @throws IOException if there was a problem counting the lines.
     */
    void count(final char[] characters, final FileStats fileStats) throws IOException {
        count(new CharData(characters), fileStats);
    }

    /**
     * Performs the counting of lines in the data specified in the constructor. Stats will be added to the specified
     * stats object. Note that the counts are added to any existing counts in the specified stats object.
     *
     * @param data Text to be counted
     * @param fileStats Object to collect the line count stats.
     * @throws IOException if there was a problem counting the lines.
     */
    void count(final CharData data, final FileStats fileStats) throws IOException {
        this.quote = null;
        this.quoteType = NORMAL;
        this.commentStack.clear();

        if (this.language == Language.Jupyter) {
            countJupyter(data, fileStats);
            return;
        }

        final Matcher matcher = data.matcher(this.language.getImportantSyntax());
        final boolean found = matcher.find();
        if (!found) {
            countComplex(data, fileStats);
        }

        final int syntaxStart = matcher.start();
        final int lineStart = data.findLineStart(syntaxStart);
        if (lineStart == 0) {
            countComplex(data, fileStats);
        }

        final CharData[] sections = data.splitAt(lineStart);
        countSimple(sections[0], fileStats);
        countComplex(sections[1], fileStats);
    }

    private void countSimple(final CharData data, final FileStats fileStats) {
        LOGGER.log(TRACE, "Simple Count on: {0}", data);

        final LanguageStats stats = fileStats.stats(this.language);
        final CharData.LineIterator lineIter = data.lineIterator();

        while (lineIter.hasNext()) {
            CharData line = lineIter.next();

            // Languages such as FORTRAN treat column positions as significant. For example, in legacy FORTRAN,
            // a "C" in the first column signifies a comment line. Therefore, do not trim lines for languages
            // where columns are significant.
            if (!this.language.isColumnSignificant()) {
                line = line.trim();
            }

            if (line.isBlank()) {
                stats.incrementBlankLines();
            } else if (this.language.isLiterate()
                    || this.language.getLineComments().stream().anyMatch(line::startsWith)) {
                stats.incrementCommentLines();
            } else {
                stats.incrementCodeLines();
            }
        }
    }

    private void countComplex(final CharData data, final FileStats fileStats) throws IOException {
        final LanguageStats stats = fileStats.stats(this.language);
        CharData.LineIterator lineIter = data.lineIterator();

        while (lineIter.hasNext()) {
            CharData line = lineIter.next();

            // Languages such as FORTRAN treat column positions as significant. For example, in legacy FORTRAN,
            // a "C" in the first column signifies a comment line. Therefore, do not trim lines for languages
            // where columns are significant.
            if (!this.language.isColumnSignificant()) {
                line = line.trim();
            }

            LOGGER.log(TRACE, line);

            if (tryCountSingleLine(line, stats)) {
                continue;
            }

            final boolean startedInComments = !this.commentStack.isEmpty()
                    || (this.config.isCountDocStrings() && this.quote != null && this.quoteType == DOC);

            final Optional<Embedding.Embedded> embeddedOpt = performMultiLineAnalysis(data, lineIter.getStart(),
                                                                                      lineIter.getEnd(), fileStats);

            if (embeddedOpt.isPresent()) {
                final Embedding.Embedded embedded = embeddedOpt.get();
                stats.addCommentLines(embedded.getCommentLines());
                stats.addCodeLines(embedded.getAdditionalCodeLines());

                lineIter = data.subSequence(embedded.getCodeEnd()).lineIterator();
                continue;
            }

            LOGGER.log(TRACE, line);

            if (this.language.isLiterate() || isCommentLine(line, startedInComments)) {
                stats.incrementCommentLines();
                LOGGER.log(TRACE, "Comment no. {0}", stats.getCommentLines());
                LOGGER.log(TRACE, "Was the comment stack empty?: {0}", !startedInComments);
            } else {
                stats.incrementCodeLines();
                LOGGER.log(TRACE, "Code no. {0}", stats.getCodeLines());
            }
        }
    }

    private void countJupyter(final CharSequence data, final FileStats fileStats) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jupyterNode = mapper.readTree(new CharSequenceReader(data));

        Optional<Language> languageOpt = Optional.empty();
        final JsonNode languageNode = jupyterNode.at(JUPYTER_LANGUAGE_PTR);
        if (!languageNode.isMissingNode()) {
            final String languageName = languageNode.asText();
            languageOpt = Language.fromName(languageName);
        }
        if (languageOpt.isEmpty()) {
            final JsonNode extensionNode = jupyterNode.at(JUPYTER_EXTENSION_PTR);
            if (!extensionNode.isMissingNode()) {
                final String extension = extensionNode.asText();
                languageOpt = Language.fromFileExtension(extension);
            }
        }
        final Language lang = languageOpt.orElse(Language.Python);

        final JsonNode cellsNode = jupyterNode.get("cells");
        if (cellsNode != null) {
            for (final JsonNode cellNode : cellsNode) {
                final JsonNode cellTypeNode = cellNode.get("cell_type");
                if (cellTypeNode != null) {
                    final String cellType = cellTypeNode.asText();
                    final JsonNode sourceNode = cellNode.get("source");
                    if (sourceNode != null) {
                        final List<String> sourceLines = new ArrayList<>();
                        for (final JsonNode sourceLineNode : sourceNode) {
                            final String sourceLine = sourceLineNode.asText();
                            sourceLines.add(sourceLine);
                        }
                        final String source = String.join("", sourceLines);
                        switch (cellType) {
                            case "markdown": {
                                final Counter counter = new Counter(Language.Markdown, this.config);
                                counter.count(source.toCharArray(), fileStats);
                                break;
                            }
                            case "code": {
                                final Counter counter = new Counter(lang, this.config);
                                counter.count(source.toCharArray(), fileStats);
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    private Optional<Embedding.Embedded> performMultiLineAnalysis(final CharData lines, final int start,
                                                                  final int end, final FileStats fileStats)
            throws IOException {
        int skip = 0;

        final Optional<Embedding.Embedded> embeddedOpt = Embedding.find(this.language, lines, start, end);

        for (int i = start; i < end; i++) {
            if (skip != 0) {
                skip--;
                continue;
            }

            final CharData window = lines.subSequence(i);
            if (window.isBlank()) {
                break;
            }

            Optional<Integer> endOfQuoteOpt = parseEndOfQuote(window);
            if (endOfQuoteOpt.isEmpty()) {
                endOfQuoteOpt = parseEndOfMultiLine(window);
            }

            if (endOfQuoteOpt.isPresent()) {
                skip = endOfQuoteOpt.get() - 1;
                continue;
            } else if (this.quote != null) {
                continue;
            }

            if (embeddedOpt.isPresent() && this.commentStack.isEmpty()) {
                final Embedding.Embedded embedded = embeddedOpt.get();
                final int embeddedStart = embedded.getEmbeddedStart();
                if (i == embeddedStart) {
                    final Counter counter = new Counter(embedded.getLanguage(), this.config);
                    counter.count(embedded.getCode(), fileStats);
                    return embeddedOpt;
                }
            }

            Optional<Integer> quoteOrMultiLine = parseQuote(window);
            if (quoteOrMultiLine.isEmpty()) {
                quoteOrMultiLine = parseMultiLineComment(window);
            }

            if (quoteOrMultiLine.isPresent()) {
                skip = quoteOrMultiLine.get() - 1;
                continue;
            }

            if (parseLineComment(window)) {
                break;
            }
        }

        return Optional.empty();
    }

    private boolean tryCountSingleLine(final CharData line, final LanguageStats stats) {
        if (parsingMode() != CODE) {
            return false;
        }

        if (line.isBlank()) {
            stats.incrementBlankLines();
            LOGGER.log(TRACE, "Blank count: {0}", stats.getBlankLines());
            return true;
        }

        if (line.matcher(this.language.getImportantSyntax()).find()) {
            return false;
        }

        LOGGER.log(TRACE, "^ Simple parseable");

        if (this.language.isLiterate() || this.language.getLineComments().stream().anyMatch(line::startsWith)) {
            stats.incrementCommentLines();
            LOGGER.log(TRACE, "Comment count: {0}", stats.getCommentLines());
        } else {
            stats.incrementCodeLines();
            LOGGER.log(TRACE, "Code count: {0}", stats.getCommentLines());
        }

        return true;
    }

    private boolean isCommentLine(final CharData line, final boolean startedInComments) {
        final CharData trimmed = line.trim();

        if (this.quote != null) {
            return this.quoteType == DOC && this.config.isCountDocStrings();
        }

        if (this.language.getDocQuotes()
                                .stream()
                                .anyMatch(bd -> line.contains(bd.end())) && startedInComments) {
            return true;
        }

        if (this.language.getLineComments()
                         .stream()
                         .anyMatch(trimmed::startsWith)
                || this.language.getAllMultiLineComments()
                                .stream()
                                .anyMatch(bd -> trimmed.startsWith(bd.start()) && trimmed.endsWith(bd.end()))) {
            return true;
        }

        if (startedInComments) {
            return true;
        }

        final CharSequence currentComment = this.commentStack.peek();
        if (currentComment == null) {
            return false;
        }

        return this.language.getAllMultiLineComments()
                            .stream()
                            .anyMatch(bd -> bd.end().contentEquals(currentComment) && trimmed.startsWith(bd.start()));
    }

    private boolean parseLineComment(final CharData window) {
        final ParsingMode mode = parsingMode();
        if (mode != CODE) {
            return false;
        }

        final Optional<String> commentOpt = this.language.getLineComments()
                                                         .stream()
                                                         .filter(window::startsWith)
                                                         .findFirst();
        if (commentOpt.isPresent()) {
            LOGGER.log(TRACE, "Start {0}", commentOpt.get());
            return true;
        }
        return false;
    }

    private Optional<Integer> parseQuote(final CharData window) {
        if (!this.commentStack.isEmpty()) {
            return Optional.empty();
        }

        final Optional<BlockDelimiter> docDelimOpt = this.language.getDocQuotes()
                                                                  .stream()
                                                                  .filter(delim -> window.startsWith(delim.start()))
                                                                  .findFirst();
        if (docDelimOpt.isPresent()) {
            final BlockDelimiter delim = docDelimOpt.get();
            LOGGER.log(TRACE, "Start doc {0}", delim.start());
            this.quote = delim.end();
            this.quoteType = DOC;
            return Optional.of(delim.start().length());
        }

        final Optional<BlockDelimiter> verbatimDelimOpt = this.language.getVerbatimQuotes()
                                                                       .stream()
                                                                       .filter(delim -> window.startsWith(delim.start()))
                                                                       .findFirst();
        if (verbatimDelimOpt.isPresent()) {
            final BlockDelimiter delim = verbatimDelimOpt.get();
            LOGGER.log(TRACE, "Start verbatim {0}", delim.start());
            this.quote = delim.end();
            this.quoteType = VERBATIM;
            return Optional.of(delim.start().length());
        }

        final Optional<BlockDelimiter> quotesOpt = this.language.getQuotes()
                                                                .stream()
                                                                .filter(delim -> window.startsWith(delim.start()))
                                                                .findFirst();
        if (quotesOpt.isPresent()) {
            final BlockDelimiter delim = quotesOpt.get();
            LOGGER.log(TRACE, "Start {0}", delim.start());
            this.quote = delim.end();
            this.quoteType = NORMAL;
            return Optional.of(delim.start().length());
        }

        return Optional.empty();
    }

    private Optional<Integer> parseEndOfQuote(final CharData window) {
        if (parsingMode() == STRING && this.quote != null && window.startsWith(this.quote)) {
            LOGGER.log(TRACE, "End {0}", this.quote);
            return Optional.of(this.quote.length());
        }

        if (this.quoteType != VERBATIM && window.startsWith("\\\\")) {
            return Optional.of(2);
        }

        if (this.quoteType != VERBATIM
                && window.startsWith("\\")
                && this.language.getQuotes()
                                .stream()
                                .anyMatch(delim -> window.subSequence(1)
                                                         .startsWith(delim.start()))) {
            // Tell the state machine to skip the next character because it has been escaped if the
            // string is not a verbatim string.
            return Optional.of(2);
        }

        return Optional.empty();
    }

    private Optional<Integer> parseMultiLineComment(final CharData window) {
        if (this.quote != null) {
            return Optional.empty();
        }

        final Stream<BlockDelimiter> commentDelims = Stream.concat(this.language.getMultiLineComments().stream(),
                                                                   this.language.getNestedComments().stream());
        return commentDelims.filter(delim -> window.startsWith(delim.start()))
                            .map(delim -> {
                                if (this.commentStack.isEmpty()
                                        || this.language.isNestable()
                                        || this.language.getNestedComments().contains(delim)) {
                                    this.commentStack.push(delim.end());

                                    if (LOGGER.isLoggable(TRACE) && this.language.isNestable()) {
                                        LOGGER.log(TRACE, "Start nested {0}", delim.start());
                                    } else {
                                        LOGGER.log(TRACE, "Start {0}", delim.start());
                                    }
                                }

                                return delim.start().length();
                            })
                            .findFirst();
    }

    private Optional<Integer> parseEndOfMultiLine(final CharData window) {
        final CharSequence endComment = this.commentStack.peek();
        if (endComment == null) {
            return Optional.empty();
        }

        if (window.startsWith(endComment)) {
            this.commentStack.pop();

            if (LOGGER.isLoggable(TRACE)) {
                if (this.commentStack.isEmpty()) {
                    LOGGER.log(TRACE, "End {0}", endComment);
                } else {
                    LOGGER.log(TRACE, "Emd {0}. Still in comments.", endComment);
                }
            }

            return Optional.of(endComment.length());
        }

        return Optional.empty();
    }

    private ParsingMode parsingMode() {
        if (this.quote == null && this.commentStack.isEmpty()) {
            return CODE;
        }
        if (this.quote != null) {
            return STRING;
        }
        return COMMENT;
    }
}
