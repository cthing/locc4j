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
import org.cthing.annotations.AccessForTesting;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    static class State {
        @Nullable
        CharSequence quote;
        QuoteType quoteType = NORMAL;
        final LinkedList<CharSequence> commentStack = new LinkedList<>();
    }

    private static final JsonPointer JUPYTER_LANGUAGE_PTR = JsonPointer.compile("/metadata/kernelspec/language");
    private static final JsonPointer JUPYTER_EXTENSION_PTR = JsonPointer.compile("/metadata/language_info/file_extension");

    private final Language language;
    private final Config config;
    private final State state;

    Counter(final Language language, final Config config) {
        this(language, config, new State());
    }

    @AccessForTesting
    Counter(final Language language, final Config config, final State state) {
        this.language = language;
        this.config = config;
        this.state = state;
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
        this.state.quote = null;
        this.state.quoteType = NORMAL;
        this.state.commentStack.clear();

        if (this.language == Language.Jupyter) {
            countJupyter(data, fileStats);
            return;
        }

        final Matcher matcher = data.matcher(this.language.getImportantSyntax());
        final boolean found = matcher.find();
        if (!found) {
            countComplex(data, fileStats);
            return;
        }

        final int syntaxStart = matcher.start();
        final int lineStart = data.findLineStart(syntaxStart);
        if (lineStart == 0) {
            countComplex(data, fileStats);
            return;
        }

        final CharData[] sections = data.splitAt(lineStart);
        countSimple(sections[0], fileStats);
        countComplex(sections[1], fileStats);
    }

    private void countSimple(final CharData data, final FileStats fileStats) {
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
                stats.blankLines++;
            } else if (this.language.isLiterate()
                    || this.language.getLineComments().stream().anyMatch(line::startsWith)) {
                stats.commentLines++;
            } else {
                stats.codeLines++;
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

            if (tryCountSingleLine(line, stats)) {
                continue;
            }

            final boolean startedInComments = !this.state.commentStack.isEmpty()
                    || (this.config.isCountDocStrings() && this.state.quote != null && this.state.quoteType == DOC);

            final Optional<Embedding.Embedded> embeddedOpt = performMultiLineAnalysis(data, lineIter.getStart(),
                                                                                      lineIter.getEnd(), fileStats);

            if (embeddedOpt.isPresent()) {
                final Embedding.Embedded embedded = embeddedOpt.get();
                stats.commentLines += embedded.getCommentLines();
                stats.codeLines += embedded.getAdditionalCodeLines();

                lineIter = data.lineIterator(embedded.getCodeEnd());
                continue;
            }

            if (this.language.isLiterate() || isComment(line, startedInComments)) {
                stats.commentLines++;
            } else {
                stats.codeLines++;
            }
        }
    }

    private void countJupyter(final CharData data, final FileStats fileStats) throws IOException {
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

        countSimple(data, fileStats);
        final LanguageStats jupyterStats = fileStats.stats(Language.Jupyter);
        final LanguageStats markdownStats = fileStats.stats(Language.Markdown);
        final LanguageStats langStats = fileStats.stats(lang);
        jupyterStats.codeLines -= (markdownStats.getTotalLines() + langStats.getTotalLines());
    }

    /**
     * Processes the specified character data to handle multiline constructs and embedded languages.
     *
     * @param lines Character data to analyze
     * @param start Location in the data to start analyzing (inclusive)
     * @param end Location in the data to end analyzing (exclusive)
     * @param fileStats Line counts
     * @return Information about an embedded language, if found
     * @throws IOException If there was a problem processing the character data.
     */
    @AccessForTesting
    Optional<Embedding.Embedded> performMultiLineAnalysis(final CharData lines, final int start,
                                                          final int end, final FileStats fileStats)
            throws IOException {
        int skip = 0;

        final Optional<Embedding.Embedded> embeddedOpt = Embedding.find(this.language, lines, start, end);

        for (int i = start; i < end; i++) {
            if (skip != 0) {
                skip--;
                continue;
            }

            // 1) The data is empty or whitespace.
            final CharData window = lines.subSequence(i);
            if (window.isBlank()) {
                break;
            }

            // 2) Quote end delimiter
            Optional<Integer> endOfQuoteOpt = parseEndOfQuote(window);
            if (endOfQuoteOpt.isEmpty()) {
                endOfQuoteOpt = parseEndOfMultiLine(window);
            }

            if (endOfQuoteOpt.isPresent()) {
                skip = endOfQuoteOpt.get() - 1;
                continue;
            } else if (this.state.quote != null) {
                continue;
            }

            // 3) Embedded language
            if (embeddedOpt.isPresent() && this.state.commentStack.isEmpty()) {
                final Embedding.Embedded embedded = embeddedOpt.get();
                final int embeddedStart = embedded.getEmbeddedStart();
                if (i == embeddedStart) {
                    final Counter counter = new Counter(embedded.getLanguage(), this.config);
                    counter.count(embedded.getCode(), fileStats);
                    return embeddedOpt;
                }
            }

            // 4) Quote start delimiter
            Optional<Integer> quoteOrMultiLine = parseQuote(window);
            if (quoteOrMultiLine.isEmpty()) {
                quoteOrMultiLine = parseMultiLineComment(window);
            }

            if (quoteOrMultiLine.isPresent()) {
                skip = quoteOrMultiLine.get() - 1;
                continue;
            }

            // 5) Single line comment
            if (isLineComment(window)) {
                break;
            }
        }

        // 6) Nothing of interest
        return Optional.empty();
    }

    /**
     * Attempts to count the specified character data as a line. If the line contains characters that introduce
     * multiline constructs (e.g. strings, comments), it cannot be counted yet.
     *
     * @param line Character data to count
     * @param stats Line counts to update
     * @return {@code true} if the line could be counted.
     */
    @AccessForTesting
    boolean tryCountSingleLine(final CharData line, final LanguageStats stats) {
        if (parsingMode() != CODE) {
            return false;
        }

        if (line.isBlank()) {
            stats.blankLines++;
            return true;
        }

        if (line.matcher(this.language.getImportantSyntax()).find()) {
            return false;
        }

        if (this.language.isLiterate() || this.language.getLineComments().stream().anyMatch(line::startsWith)) {
            stats.commentLines++;
        } else {
            stats.codeLines++;
        }

        return true;
    }

    /**
     * Determines whether the specified line is a comment.
     *
     * @param line Character data to test
     * @param startedInComments Indicates whether already in a multiline comment
     * @return {@code true} if the line is a comment.
     */
    @AccessForTesting
    boolean isComment(final CharData line, final boolean startedInComments) {
        final CharData trimmed = line.trim();

        // 1) If in a doc string, count it as a comment if configured to do so
        if (this.state.quote != null) {
            return this.state.quoteType == DOC && this.config.isCountDocStrings();
        }

        // 2) If in a multiline comment and the line contains the doc string end delimiter
        if (this.language.getDocQuotes()
                                .stream()
                                .anyMatch(bd -> line.contains(bd.end())) && startedInComments) {
            return true;
        }

        // 3) If this is a line comment or the line contains a single line comment using multiline syntax
        if (this.language.getLineComments()
                         .stream()
                         .anyMatch(trimmed::startsWith)
                || this.language.getAllMultiLineComments()
                                .stream()
                                .anyMatch(bd -> trimmed.startsWith(bd.start()) && trimmed.endsWith(bd.end()))) {
            return true;
        }

        // 4) If in a multiline comment
        if (startedInComments) {
            return true;
        }

        // 5) If there is an open multiline comment
        final CharSequence currentComment = this.state.commentStack.peek();
        if (currentComment == null) {
            return false;
        }

        // 6) If the line starts a multiline comment
        return this.language.getAllMultiLineComments()
                            .stream()
                            .anyMatch(bd -> bd.end().contentEquals(currentComment) && trimmed.startsWith(bd.start()));
    }

    /**
     * Parses the specified character data to determine whether it represents a line comment.
     *
     * @param window Character data to parse
     * @return {@code true} if the specified character data represents a line comment
     */
    @AccessForTesting
    boolean isLineComment(final CharData window) {
        return parsingMode() == CODE && this.language.getLineComments()
                                                     .stream()
                                                     .anyMatch(window::startsWith);
    }

    /**
     * Parses the specified character data for the start of a quote.
     *
     * @param window Character data to parse
     * @return Length of the quote start delimiter if found.
     */
    @AccessForTesting
    Optional<Integer> parseQuote(final CharData window) {
        if (!this.state.commentStack.isEmpty()) {
            return Optional.empty();
        }

        final Optional<BlockDelimiter> docDelimOpt = this.language.getDocQuotes()
                                                                  .stream()
                                                                  .filter(delim -> window.startsWith(delim.start()))
                                                                  .findFirst();
        if (docDelimOpt.isPresent()) {
            final BlockDelimiter delim = docDelimOpt.get();
            this.state.quote = delim.end();
            this.state.quoteType = DOC;
            return Optional.of(delim.start().length());
        }

        final Optional<BlockDelimiter> verbatimDelimOpt = this.language.getVerbatimQuotes()
                                                                       .stream()
                                                                       .filter(delim -> window.startsWith(delim.start()))
                                                                       .findFirst();
        if (verbatimDelimOpt.isPresent()) {
            final BlockDelimiter delim = verbatimDelimOpt.get();
            this.state.quote = delim.end();
            this.state.quoteType = VERBATIM;
            return Optional.of(delim.start().length());
        }

        final Optional<BlockDelimiter> quotesOpt = this.language.getQuotes()
                                                                .stream()
                                                                .filter(delim -> window.startsWith(delim.start()))
                                                                .findFirst();
        if (quotesOpt.isPresent()) {
            final BlockDelimiter delim = quotesOpt.get();
            this.state.quote = delim.end();
            this.state.quoteType = NORMAL;
            return Optional.of(delim.start().length());
        }

        return Optional.empty();
    }

    /**
     * Parses the specified character data for the end of a quote.
     *
     * @param window Character data to parse
     * @return The number of characters to advance if the end of a quote has been found or escaping is being used
     *      outside a verbatim string.
     */
    @AccessForTesting
    Optional<Integer> parseEndOfQuote(final CharData window) {
        //noinspection DataFlowIssue
        if (parsingMode() == STRING && window.startsWith(this.state.quote)) {
            final CharSequence quote = this.state.quote;
            this.state.quote = null;
            return Optional.of(quote.length());
        }

        if (this.state.quoteType != VERBATIM && window.startsWith("\\\\")) {
            return Optional.of(2);
        }

        if (this.state.quoteType != VERBATIM
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

    /**
     * Parses the specified character data for the start of a multiline comment.
     *
     * @param window Character data to parse
     * @return The length of the comment start delimiter if one is found.
     */
    @AccessForTesting
    Optional<Integer> parseMultiLineComment(final CharData window) {
        if (this.state.quote != null) {
            return Optional.empty();
        }

        final Stream<BlockDelimiter> commentDelims = Stream.concat(this.language.getMultiLineComments().stream(),
                                                                   this.language.getNestedComments().stream());
        return commentDelims.filter(delim -> window.startsWith(delim.start()))
                            .map(delim -> {
                                if (this.state.commentStack.isEmpty()
                                        || this.language.isNestable()
                                        || this.language.getNestedComments().contains(delim)) {
                                    this.state.commentStack.push(delim.end());
                                }

                                return delim.start().length();
                            })
                            .findFirst();
    }

    /**
     * Parses the specified character data for the presence of a multiline comment end delimiter.
     *
     * @param window Character data to parse
     * @return The length of the comment end delimiter if one is found.
     */
    @AccessForTesting
    Optional<Integer> parseEndOfMultiLine(final CharData window) {
        final CharSequence endComment = this.state.commentStack.peek();
        if (endComment == null) {
            return Optional.empty();
        }

        if (window.startsWith(endComment)) {
            this.state.commentStack.pop();
            return Optional.of(endComment.length());
        }

        return Optional.empty();
    }

    /**
     * Indicates the current state of parsing. The parser is within a string, comment or
     * code.
     *
     * @return Current parsing state.
     */
    @AccessForTesting
    ParsingMode parsingMode() {
        if (this.state.quote == null && this.state.commentStack.isEmpty()) {
            return CODE;
        }
        if (this.state.quote != null) {
            return STRING;
        }
        return COMMENT;
    }
}
