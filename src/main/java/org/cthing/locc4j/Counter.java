/*
 * Copyright 2023 C Thing Software
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

import java.util.LinkedList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

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
     */
    void count(final char[] characters, final FileStats fileStats) {
        this.quote = null;
        this.quoteType = NORMAL;
        this.commentStack.clear();

        final CharData data = new CharData(characters);
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

    private void countComplex(final CharData data, final FileStats fileStats) {
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

            LOGGER.log(TRACE, line);

            if (tryCountSingleLine(line, stats)) {
                continue;
            }

            final boolean startedInComments = !this.commentStack.isEmpty()
                    || (this.config.isCountDocStrings() && this.quote != null && this.quoteType == DOC);
        }
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
