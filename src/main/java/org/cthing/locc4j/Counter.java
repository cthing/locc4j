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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.annotation.WillNotClose;

import org.apache.commons.io.IOUtils;
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
public class Counter {

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


    /**
     * Counting state.
     */
    static class State {
        @Nullable
        CharSequence quote;
        QuoteType quoteType = NORMAL;
        final LinkedList<CharSequence> commentStack = new LinkedList<>();

        /**
         * Resets the state to its initial setting.
         */
        void reset() {
            this.quote = null;
            this.quoteType = NORMAL;
            this.commentStack.clear();
        }
    }


    /**
     * Singleton provider for Jupyter JSON parsing. Construction of the Object Mapper is very expensive but it
     * is thread safe so one can be used for all JSON parsing.
     */
    private static final class ObjectMapperProvider {
        private static final class InstanceHolder {
            private static final ObjectMapper INSTANCE = new ObjectMapper();
        }

        private ObjectMapperProvider() {
        }

        public static ObjectMapper getInstance() {
            return InstanceHolder.INSTANCE;
        }
    }

    private static final JsonPointer JUPYTER_LANGUAGE_PTR = JsonPointer.compile("/metadata/kernelspec/language");
    private static final JsonPointer JUPYTER_EXTENSION_PTR = JsonPointer.compile("/metadata/language_info/file_extension");

    private final Language language;
    private final State state;
    private boolean countDocStrings = true;

    /**
     * Constructs a counter for the specified language.
     *
     * @param language Language for the file to be counted
     */
    public Counter(final Language language) {
        this(language, new State());
    }

    /**
     * Constructs a counter for the specified language.
     *
     * @param language Language for the file to be counted
     * @param state Counting state that can be specified for testing purposes
     */
    @AccessForTesting
    Counter(final Language language, final State state) {
        this.language = language;
        this.state = state;
    }

    /**
     * Sets whether to count documentation string as comments or ignore them.
     *
     * @param enable {@code true} to count documentation strings as comments or {@code false} to ignore them.
     *      The default is to count documentation strings as comments.
     * @return This counter.
     */
    public Counter countDocStrings(final boolean enable) {
        this.countDocStrings = enable;
        return this;
    }

    /**
     * Performs the counting of lines in the input stream.
     *
     * @param inputStream Stream providing the text to be counted
     * @return Map of the languages in the data and their line counts
     * @throws IOException if there was a problem counting the lines.
     */
    @WillNotClose
    public Map<Language, Stats> count(final InputStream inputStream) throws IOException {
        return count(IOUtils.toCharArray(inputStream, StandardCharsets.UTF_8));
    }

    /**
     * Performs the counting of lines in the specified data.
     *
     * @param characters Text to be counted
     * @return Map of the languages in the data and their line counts
     * @throws IOException if there was a problem counting the lines.
     */
    public Map<Language, Stats> count(final char[] characters) throws IOException {
        final Map<Language, Stats> languageMap = new EnumMap<>(Language.class);
        count(new CharData(characters), languageMap);
        return Collections.unmodifiableMap(languageMap);
    }

    /**
     * Creates a new counter with the same configuration as this counter.
     *
     * @param lang Language for the counter
     * @return Newly created counter
     */
    private Counter newCounter(final Language lang) {
        return new Counter(lang).countDocStrings(this.countDocStrings);
    }

    /**
     * Performs the counting of lines in the specified data. Stats will be added to the specified stats object.
     * Note that the counts are added to any existing counts in the specified stats object.
     *
     * @param data Text to be counted
     * @param languageStats Map to collect the line count stats for all counted languages.
     * @throws IOException if there was a problem counting the lines.
     */
    private void count(final CharData data, final Map<Language, Stats> languageStats) throws IOException {
        this.state.reset();

        if (this.language == Language.Jupyter) {
            countJupyter(data, languageStats);
            return;
        }

        final Stats stats = languageStats.computeIfAbsent(this.language, lang -> new Stats());
        CharData.LineIterator lineIter = data.lineIterator();

        while (lineIter.hasNext()) {
            CharData line = lineIter.next();

            // Languages such as FORTRAN treat column positions as significant. For example, in legacy FORTRAN,
            // a "C" in the first column signifies a comment line. Therefore, do not trim lines for languages
            // where columns are significant.
            if (!this.language.isColumnSignificant()) {
                line = line.trim();
            }

            if (parseSingleLine(line, stats)) {
                continue;
            }

            final boolean startedInComments = !this.state.commentStack.isEmpty()
                    || (this.countDocStrings && this.state.quote != null && this.state.quoteType == DOC);

            final Embedding.Embedded embedded = performMultiLineAnalysis(data, lineIter.getStart(),
                                                                         lineIter.getEnd(), languageStats);

            if (embedded != null) {
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

    /**
     * Reads the specified Jupyter notebook data and counts the lines of JSON and embedded languages.
     *
     * @param data Jupyter notebook data
     * @param languageStats Map to collect the line count stats for all counted languages.
     * @throws IOException if there was a problem reading the data
     */
    private void countJupyter(final CharData data, final Map<Language, Stats> languageStats) throws IOException {
        final ObjectMapper mapper = ObjectMapperProvider.getInstance();
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
                        final StringBuilder source = new StringBuilder();
                        sourceNode.forEach(node -> source.append(node.asText()));
                        switch (cellType) {
                            case "markdown": {
                                final Counter counter = newCounter(Language.Markdown);
                                counter.count(new CharData(source.toString().toCharArray()), languageStats);
                                break;
                            }
                            case "code": {
                                final Counter counter = newCounter(lang);
                                counter.count(new CharData(source.toString().toCharArray()), languageStats);
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
            }
        }

        final Stats markdownStats = languageStats.getOrDefault(Language.Markdown, new Stats());
        final Stats langStats = languageStats.getOrDefault(lang, new Stats());
        final Stats jupyterStats = languageStats.computeIfAbsent(Language.Jupyter, l -> new Stats());
        jupyterStats.codeLines = data.countLines() - (markdownStats.getTotalLines() + langStats.getTotalLines());
    }

    /**
     * Processes the specified character data to handle multiline constructs and embedded languages.
     *
     * @param lines Character data to analyze
     * @param start Location in the data to start analyzing (inclusive)
     * @param end Location in the data to end analyzing (exclusive)
     * @param languageStats Line counts
     * @return Information about an embedded language, if found
     * @throws IOException If there was a problem processing the character data.
     */
    @AccessForTesting
    @Nullable
    Embedding.Embedded performMultiLineAnalysis(final CharData lines, final int start, final int end,
                                                final Map<Language, Stats> languageStats) throws IOException {
        final Embedding.Embedded embedded = Embedding.find(this.language, lines, start, end);

        int skip = 0;
        for (int i = start; i < end; i += skip + 1, skip = 0) {
            // Numbering is referenced in unit test cases.

            // 1) The data is empty or whitespace.
            final CharData window = lines.subSequence(i);
            if (window.isBlank()) {
                break;
            }

            // 2) Quote end delimiter
            int endOfQuote = parseEndOfQuote(window);
            if (endOfQuote == 0) {
                endOfQuote = parseEndOfMultiLine(window);
            }
            if (endOfQuote > 0) {
                skip = endOfQuote - 1;
                continue;
            }
            if (this.state.quote != null) {
                continue;
            }

            // 3) Embedded language
            if (embedded != null && this.state.commentStack.isEmpty()) {
                final int embeddedStart = embedded.getEmbeddedStart();
                if (i == embeddedStart) {
                    final Counter counter = newCounter(embedded.getLanguage());
                    counter.count(embedded.getCode(), languageStats);
                    return embedded;
                }
            }

            // 4) Quote start delimiter
            int quoteOrMultiLine = parseQuote(window);
            if (quoteOrMultiLine == 0) {
                quoteOrMultiLine = parseMultiLineComment(window);
            }

            if (quoteOrMultiLine > 0) {
                skip = quoteOrMultiLine - 1;
                continue;
            }

            // 5) Single line comment
            if (isLineComment(window)) {
                break;
            }
        }

        // 6) Nothing of interest
        return null;
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
        // Numbering is referenced in unit test cases.

        final CharData trimmed = line.trim();

        // 1) If in a doc string, count it as a comment if configured to do so
        if (this.state.quote != null) {
            return this.state.quoteType == DOC && this.countDocStrings;
        }

        // 2) If in a multiline comment and the line contains the doc string end delimiter
        if (this.language.isDocQuote(bd -> line.contains(bd.end())) && startedInComments) {
            return true;
        }

        // 3) If this is a line comment or the line contains a single line comment using multiline syntax
        if (this.language.isLineComment(trimmed::startsWith)
                || this.language.isAnyMultiLineComment(bd -> trimmed.startsWith(bd.start()) && trimmed.endsWith(bd.end()))) {
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
        return this.language.isAnyMultiLineComment(bd -> bd.end().contentEquals(currentComment) && trimmed.startsWith(bd.start()));
    }

    /**
     * Parses the specified character data to determine whether it represents a line comment.
     *
     * @param window Character data to parse
     * @return {@code true} if the specified character data represents a line comment
     */
    @AccessForTesting
    boolean isLineComment(final CharData window) {
        return parsingMode() == CODE && this.language.isLineComment(window::startsWith);
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
    boolean parseSingleLine(final CharData line, final Stats stats) {
        // If in a string or block comment, single line parsing cannot be used.
        if (parsingMode() != CODE) {
            return false;
        }

        // If the line is blank, count it.
        if (line.isBlank()) {
            stats.blankLines++;
            return true;
        }

        // If the line contains important syntax (e.g. a <script> tag), single line parsing cannot be used.
        final Pattern importantSyntax = this.language.getImportantSyntax();
        if (importantSyntax != null && line.matcher(importantSyntax).find()) {
            return false;
        }

        // Count the line
        if (this.language.isLiterate() || this.language.isLineComment(line::startsWith)) {
            stats.commentLines++;
        } else {
            stats.codeLines++;
        }

        return true;
    }

    /**
     * Parses the specified character data for the start of a quote.
     *
     * @param window Character data to parse
     * @return Length of the quote start delimiter if found or 0 if not found.
     */
    @AccessForTesting
    int parseQuote(final CharData window) {
        if (!this.state.commentStack.isEmpty()) {
            return 0;
        }

        final BlockDelimiter docDelim = this.language.findDocQuote(delim -> window.startsWith(delim.start()));
        if (docDelim != null) {
            this.state.quote = docDelim.end();
            this.state.quoteType = DOC;
            return docDelim.start().length();
        }

        final BlockDelimiter verbatimDelim = this.language.findVerbatimQuote(delim -> window.startsWith(delim.start()));
        if (verbatimDelim != null) {
            this.state.quote = verbatimDelim.end();
            this.state.quoteType = VERBATIM;
            return verbatimDelim.start().length();
        }

        final BlockDelimiter quoteDelim = this.language.findQuote(delim -> window.startsWith(delim.start()));
        if (quoteDelim != null) {
            this.state.quote = quoteDelim.end();
            this.state.quoteType = NORMAL;
            return quoteDelim.start().length();
        }

        return 0;
    }

    /**
     * Parses the specified character data for the end of a quote.
     *
     * @param window Character data to parse
     * @return The number of characters to advance if the end of a quote has been found or escaping is being used
     *      outside a verbatim string.
     */
    @AccessForTesting
    int parseEndOfQuote(final CharData window) {
        //noinspection DataFlowIssue
        if (parsingMode() == STRING && window.startsWith(this.state.quote)) {
            final CharSequence quote = this.state.quote;
            this.state.quote = null;
            return quote.length();
        }

        if (this.state.quoteType != VERBATIM && window.startsWith("\\\\")) {
            return 2;
        }

        if (this.state.quoteType != VERBATIM
                && window.startsWith("\\")
                && this.language.isQuote(delim -> window.subSequence(1).startsWith(delim.start()))) {
            // Tell the state machine to skip the next character because it has been escaped if the
            // string is not a verbatim string.
            return 2;
        }

        return 0;
    }

    /**
     * Parses the specified character data for the start of a multiline comment.
     *
     * @param window Character data to parse
     * @return The length of the comment start delimiter if one is found or 0 if not found
     */
    @AccessForTesting
    int parseMultiLineComment(final CharData window) {
        if (this.state.quote != null) {
            return 0;
        }

        final BlockDelimiter commentDelim = this.language.findAnyMultiLineComment(delim -> window.startsWith(delim.start()));
        if (commentDelim == null) {
            return 0;
        }
        if (this.state.commentStack.isEmpty()
                || this.language.isNestable()
                || this.language.isNestedComment(delim -> delim.equals(commentDelim))) {
            this.state.commentStack.push(commentDelim.end());
        }

        return commentDelim.start().length();
    }

    /**
     * Parses the specified character data for the presence of a multiline comment end delimiter.
     *
     * @param window Character data to parse
     * @return The length of the comment end delimiter if one is found.
     */
    @AccessForTesting
    int parseEndOfMultiLine(final CharData window) {
        final CharSequence endComment = this.state.commentStack.peek();
        if (endComment == null) {
            return 0;
        }

        if (window.startsWith(endComment)) {
            this.state.commentStack.pop();
            return endComment.length();
        }

        return 0;
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
