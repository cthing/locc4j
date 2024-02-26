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
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("MethodOnlyUsedFromInnerClass")
public class CounterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterTest.class);

    private static long countingTime;
    private static int totalLines;

    private final Counter.State state = new Counter.State();

    @AfterAll
    public static void allDone() {
        LOGGER.info(() -> String.format("Total lines:   %d\n      Counting time: %d ms\n      Velocity:      %d lines/ms",
                                        totalLines, countingTime, Math.round(totalLines / countingTime)));
    }

    @Nested
    @DisplayName("parsingMode method")
    class ParsingModeTest {
        @Test
        @DisplayName("Parsing in code mode")
        public void testCodeMode() {
            assertThat(makeCounter().parsingMode()).isEqualTo(Counter.ParsingMode.CODE);
        }

        @Test
        @DisplayName("Parsing in string mode")
        public void testStringMode() {
            stack("a");
            quote("b");
            assertThat(makeCounter().parsingMode()).isEqualTo(Counter.ParsingMode.STRING);
        }

        @Test
        @DisplayName("Parsing in comment mode")
        public void testStringComment() {
            stack("a");
            assertThat(makeCounter().parsingMode()).isEqualTo(Counter.ParsingMode.COMMENT);
        }
    }

    @Nested
    @DisplayName("parseEndOfMultiLine method")
    class ParseEndOfMultiLineTest {
        @Test
        @DisplayName("Ignore comment end when not in multiline comment")
        public void testNoComment() {
            assertThat(makeCounter().parseEndOfMultiLine(data("*/"))).isZero();
        }

        @Test
        @DisplayName("Ignore comment end when mismatched delimiters")
        public void testWrongDelimiter() {
            stack("a");
            assertThat(makeCounter().parseEndOfMultiLine(data("*/"))).isZero();
        }

        @Test
        @DisplayName("Found comment end delimiter")
        public void testFoundEndDelimiter() {
            stack("*/");
            assertThat(makeCounter().parseEndOfMultiLine(data("*/"))).isEqualTo(2);
            assertThat(stack()).isEmpty();
        }
    }

    @Nested
    @DisplayName("parseMultiLineComment method")
    class ParseMultiLineCommentTest {
        @Test
        @DisplayName("Ignore start of a multiline comment occurring within a string")
        public void testInString() {
            quote("\"");
            assertThat(makeCounter().parseMultiLineComment(data("/*"))).isZero();
        }

        @Test
        @DisplayName("Found the start of a multiline comment")
        public void testFound() {
            assertThat(makeCounter().parseMultiLineComment(data("/* This is a test"))).isEqualTo(2);
            assertThat(stack()).containsExactly("*/");
        }

        @Test
        @DisplayName("Found nested comment in language that ignores nested comments")
        public void testIgnoreNested() {
            stack("*/");
            assertThat(makeCounter().parseMultiLineComment(data("/*"))).isEqualTo(2);
            assertThat(stack()).containsExactly("*/");
        }

        @Test
        @DisplayName("Found nested comment in language allowing nested comments")
        public void testFoundNestedNestable() {
            stack("*/");
            assertThat(makeCounter(Language.Kotlin, true).parseMultiLineComment(data("/*"))).isEqualTo(2);
            assertThat(stack()).containsExactly("*/", "*/");
        }

        @Test
        @DisplayName("Found nested comment using dedicated nested comment syntax")
        public void testFoundNestedSyntaxNestable() {
            stack("*/");
            assertThat(makeCounter(Language.D, true).parseMultiLineComment(data("/+"))).isEqualTo(2);
            assertThat(stack()).containsExactly("+/", "*/");
        }
    }

    @Nested
    @DisplayName("parseEndOfQuote method")
    class ParseEndOfQuoteTest {
        @Test
        @DisplayName("Found end of string")
        public void testFoundInString() {
            quote("\"");
            assertThat(makeCounter().parseEndOfQuote(data("\""))).isEqualTo(1);
            assertThat(quote()).isNull();
        }

        @Test
        @DisplayName("Found a double backslash")
        public void testFoundInStringDoubleBackslash() {
            quote("\"");
            assertThat(makeCounter().parseEndOfQuote(data("\\\\"))).isEqualTo(2);
            assertThat(quote()).isNotNull();
        }

        @Test
        @DisplayName("Ignore a double backslash in verbatim")
        public void testIgnoreInVerbatimDoubleBackslash() {
            quote("\"");
            quoteType(Counter.QuoteType.VERBATIM);
            assertThat(makeCounter().parseEndOfQuote(data("\\\\"))).isZero();
            assertThat(quote()).isNotNull();
        }

        @Test
        @DisplayName("Found escaped quote")
        public void testFoundEscapedQuote() {
            quote("\"");
            assertThat(makeCounter().parseEndOfQuote(data("\\\""))).isEqualTo(2);
            assertThat(quote()).isNotNull();
        }

        @Test
        @DisplayName("Ignore escaped quote in verbatim")
        public void testIgnoreInVerbatimEscapedQuote() {
            quote("\"");
            quoteType(Counter.QuoteType.VERBATIM);
            assertThat(makeCounter().parseEndOfQuote(data("\\\""))).isZero();
            assertThat(quote()).isNotNull();
        }

        @Test
        @DisplayName("Not found")
        public void testNotFound() {
            quote("\"");
            assertThat(makeCounter().parseEndOfQuote(data("abcd"))).isZero();
            assertThat(quote()).isNotNull();
        }
    }

    @Nested
    @DisplayName("parseQuote method")
    class ParseQuoteTest {
        @Test
        @DisplayName("Ignore quotes in multiline comment")
        public void testIgnoreInComment() {
            stack("*/");
            assertThat(makeCounter().parseQuote(data("\"Hello"))).isZero();
            assertThat(quote()).isNull();
            assertThat(quoteType()).isEqualTo(Counter.QuoteType.NORMAL);
        }

        @Test
        @DisplayName("Found document string")
        public void testFoundDocString() {
            assertThat(makeCounter(Language.Python, true).parseQuote(data("\"\"\"\nHello world"))).isEqualTo(3);
            assertThat(quote()).isEqualTo("\"\"\"");
            assertThat(quoteType()).isEqualTo(Counter.QuoteType.DOC);
        }

        @Test
        @DisplayName("Found verbatim string")
        public void testFoundVerbatimString() {
            assertThat(makeCounter(Language.Rust, true).parseQuote(data("r##\"\nHello world"))).isEqualTo(4);
            assertThat(quote()).isEqualTo("\"##");
            assertThat(quoteType()).isEqualTo(Counter.QuoteType.VERBATIM);
        }

        @Test
        @DisplayName("Found normal string")
        public void testFoundNormalString() {
            assertThat(makeCounter().parseQuote(data("\"Hello world"))).isEqualTo(1);
            assertThat(quote()).isEqualTo("\"");
            assertThat(quoteType()).isEqualTo(Counter.QuoteType.NORMAL);
        }

        @Test
        @DisplayName("Not found")
        public void testNotFound() {
            assertThat(makeCounter().parseQuote(data("Hello world"))).isZero();
            assertThat(quote()).isNull();
            assertThat(quoteType()).isEqualTo(Counter.QuoteType.NORMAL);
        }
    }

    @Nested
    @DisplayName("isLineComment method")
    class IsLineCommentTest {
        @Test
        @DisplayName("Not in code mode - comments ignored")
        public void testNotCode() {
            quote("\"");
            assertThat(makeCounter().isLineComment(data("// Hello world"))).isFalse();
        }

        @Test
        @DisplayName("No line comment in code")
        public void testNotComment() {
            assertThat(makeCounter().isLineComment(data("Hello world"))).isFalse();
        }

        @Test
        @DisplayName("Code starts with line comment")
        public void testFound() {
            assertThat(makeCounter().isLineComment(data("// Hello world"))).isTrue();
        }
    }

    @Nested
    @DisplayName("isComment method")
    class IsCommentTest {
        // Test case numbers refer to numbered comments in the body of the method. Letters
        // refer to the logical combinations of the parameters in those test cases.

        @Test
        @DisplayName("Count doc string as comment")
        public void testCase1A() {
            quote("*/");
            quoteType(Counter.QuoteType.DOC);
            assertThat(makeCounter(Language.Python, true).isComment(data("hello"), true)).isTrue();
        }

        @Test
        @DisplayName("Not counting doc strings")
        public void testCase1B() {
            quote("*/");
            quoteType(Counter.QuoteType.DOC);
            assertThat(makeCounter(Language.Python, false).isComment(data("hello"), true)).isFalse();
        }

        @Test
        @DisplayName("Not a doc string")
        public void testCase1C() {
            quote("*/");
            quoteType(Counter.QuoteType.NORMAL);
            assertThat(makeCounter(Language.Python, false).isComment(data("hello"), true)).isFalse();
        }

        @Test
        @DisplayName("Last line of doc comments")
        public void testCase2() {
            assertThat(makeCounter(Language.Python, true).isComment(data("hello\"\"\""), true)).isTrue();

        }

        @Test
        @DisplayName("Line comment")
        public void testCase3A() {
            assertThat(makeCounter().isComment(data("// hello"), false)).isTrue();

        }

        @Test
        @DisplayName("Multiline comment on a single line")
        public void testCase3B() {
            assertThat(makeCounter().isComment(data("/* hello */"), false)).isTrue();
        }

        @Test
        @DisplayName("In a multiline comment")
        public void testCase4() {
            assertThat(makeCounter().isComment(data("hello"), true)).isTrue();
        }

        @Test
        @DisplayName("Not in comment and nothing on the comment stack")
        public void testCase5() {
            assertThat(makeCounter().isComment(data("hello"), false)).isFalse();
        }

        @Test
        @DisplayName("Start of multiline comment")
        public void testCase6A() {
            stack("*/");
            assertThat(makeCounter().isComment(data("/* hello"), false)).isTrue();
        }

        @Test
        @DisplayName("Start of multiline comment")
        public void testCase6B() {
            stack("*/");
            assertThat(makeCounter().isComment(data("hello"), false)).isFalse();
        }
    }

    @Nested
    @DisplayName("parseSingleLine method")
    class ParseSingleLineTest {
        @Test
        @DisplayName("Not in code")
        public void testNotInCode() {
            quote("/*");

            final Stats stats = new Stats();
            assertThat(makeCounter().parseSingleLine(data("hello"), stats)).isFalse();
            verifyStats(stats, 0, 0, 0);
        }

        @Test
        @DisplayName("Blank line")
        public void testBlankLine() {
            final Stats stats = new Stats();
            assertThat(makeCounter().parseSingleLine(data("  "), stats)).isTrue();
            verifyStats(stats, 0, 0, 1);
        }

        @Test
        @DisplayName("Multiline")
        public void testMultiline() {
            final Stats stats = new Stats();
            assertThat(makeCounter().parseSingleLine(data("/*"), stats)).isFalse();
            verifyStats(stats, 0, 0, 0);
        }

        @Test
        @DisplayName("Line comment")
        public void testLineComment() {
            final Stats stats = new Stats();
            assertThat(makeCounter().parseSingleLine(data("// Hello world"), stats)).isTrue();
            verifyStats(stats, 0, 1, 0);
        }

        @Test
        @DisplayName("Code")
        public void testCode() {
            final Stats stats = new Stats();
            assertThat(makeCounter().parseSingleLine(data("int foo = 1;"), stats)).isTrue();
            verifyStats(stats, 1, 0, 0);
        }
    }

    @Nested
    @DisplayName("performMultiLineAnalysis method")
    class PerformMultiLineAnalysisTest {
        // Test case numbers refer to numbered comments in the body of the method. Letters
        // refer to the logical combinations of the parameters in those test cases.

        @Test
        public void testCase1() throws IOException {
            final String content = "    ";
            final Embedding.Embedded embedded =
                    makeCounter(Language.Html, true).performMultiLineAnalysis(data(content), 0, content.length(),
                                                                              statsMap());
            assertThat(embedded).isNull();
        }

        @Test
        public void testCase2() throws IOException {
            quote("*/");
            final String content = "   */";
            final Embedding.Embedded embedded =
                    makeCounter().performMultiLineAnalysis(data(content), 0, content.length(), statsMap());
            assertThat(embedded).isNull();
        }

        @Test
        public void testCase3() throws IOException {
            final String content = """
                                   <script>
                                   var i = 0;
                                   </script>
                                   """;
            final Embedding.Embedded embedded =
                    makeCounter(Language.Html, true).performMultiLineAnalysis(data(content), 0, content.length(),
                                                                              statsMap());
            assertThat(embedded).isNotNull();
            assertThat(embedded.getLanguage()).isEqualTo(Language.JavaScript);
            assertThat(embedded.getEmbeddedStart()).isEqualTo(0);
            assertThat(embedded.getCodeEnd()).isEqualTo(20);
            assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
            assertThat(embedded.getCode().toString()).isEqualTo("var i = 0;");
        }

        @Test
        public void testCase4A() throws IOException {
            final String content = "/*";
            final Embedding.Embedded embedded =
                    makeCounter().performMultiLineAnalysis(data(content), 0, content.length(), statsMap());
            assertThat(embedded).isNull();
        }

        @Test
        public void testCase4B() throws IOException {
            final String content = "\"Hello";
            final Embedding.Embedded embedded =
                    makeCounter().performMultiLineAnalysis(data(content), 0, content.length(), statsMap());
            assertThat(embedded).isNull();
        }

        @Test
        public void testCase5() throws IOException {
            final String content = "// Hello";
            final Embedding.Embedded embedded =
                    makeCounter().performMultiLineAnalysis(data(content), 0, content.length(), statsMap());
            assertThat(embedded).isNull();
        }

        @Test
        public void testCase6() throws IOException {
            final String content = """
                                   <p>Hello World</p>
                                   """;
            final Embedding.Embedded embedded =
                    makeCounter(Language.Html, true).performMultiLineAnalysis(data(content), 0, content.length(),
                                                                              statsMap());
            assertThat(embedded).isNull();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider.class)
    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    public void testCount(final ArgumentsAccessor accessor) throws IOException {
        final int numLanguageParams = accessor.size() - 2;
        if (numLanguageParams % 4 != 0) {
            throw new IllegalArgumentException("Incorrect number of arguments. Filename plus 4 parameters per language");
        }

        final String filename = accessor.getString(0);

        final Language primaryLanguage = accessor.get(2, Language.class);
        final Map<Language, Stats> actualStats;
        if (primaryLanguage == Language.Jupyter) {
            // Jupyter counting is very expensive and drowns out the performance of all other file counting.
            // Use this an opportunity to test the input stream based API.
            final InputStream ins = Objects.requireNonNull(getClass().getResourceAsStream("/data/" + filename));
            final Counter counter = makeCounter(primaryLanguage, accessor.getBoolean(1));
            actualStats = counter.count(ins);
        } else {
            final char[] data =
                    IOUtils.toCharArray(Objects.requireNonNull(getClass().getResourceAsStream("/data/" + filename)),
                                        StandardCharsets.UTF_8);

            final long startMillis = System.currentTimeMillis();
            final Counter counter = makeCounter(primaryLanguage, accessor.getBoolean(1));
            actualStats = counter.count(data);
            countingTime += System.currentTimeMillis() - startMillis;
            totalLines += actualStats.values().stream().mapToInt(Stats::getTotalLines).sum();
        }

        assertThat(actualStats).as("Incorrect number of languages counted").hasSize(numLanguageParams / 4);

        for (int i = 0; i < numLanguageParams; i += 4) {
            final Language language = accessor.get(i + 2, Language.class);
            final int codeLines = accessor.getInteger(i + 3);
            final int commentLines = accessor.getInteger(i + 4);
            final int blankLines = accessor.getInteger(i + 5);
            assertThat(actualStats).hasEntrySatisfying(language, languageStats -> {
                assertThat(languageStats.codeLines).as(language + ": Code lines").isEqualTo(codeLines);
                assertThat(languageStats.commentLines).as(language + ": Comment lines").isEqualTo(commentLines);
                assertThat(languageStats.blankLines).as(language + ": Blank lines").isEqualTo(blankLines);
            });
        }
    }

    @Test
    public void testCountFromString() throws IOException {
        final Counter counter = new Counter(Language.Markdown);
        final Map<Language, Stats> statsMap = counter.count("# Title\n\nHello World");
        assertThat(statsMap).hasSize(1);
        final Stats stats = statsMap.get(Language.Markdown);
        assertThat(stats).isNotNull();
        assertThat(stats.codeLines).isEqualTo(2);
        assertThat(stats.commentLines).isEqualTo(0);
        assertThat(stats.blankLines).isEqualTo(1);
    }

    private Counter makeCounter() {
        return makeCounter(Language.Java, true);
    }

    private Counter makeCounter(final Language language, final boolean countDocStrings) {
        return new Counter(language, this.state).countDocStrings(countDocStrings);
    }

    private CharData data(final String str) {
        return new CharData(str.toCharArray());
    }

    private Deque<CharSequence> stack() {
        return this.state.commentStack;
    }

    private void stack(final CharSequence c) {
        this.state.commentStack.push(c);
    }

    private void quote(@Nullable final CharSequence q) {
        this.state.quote = q;
    }

    @Nullable
    private CharSequence quote() {
        return this.state.quote;
    }

    private Counter.QuoteType quoteType() {
        return this.state.quoteType;
    }

    private void quoteType(final Counter.QuoteType type) {
        this.state.quoteType = type;
    }

    private Map<Language, Stats> statsMap() {
        return new EnumMap<>(Language.class);
    }

    private void verifyStats(final Map<Language, Stats> languageStats, final int codeLines, final int commentLines,
                             final int blankLines) {
        verifyStats(languageStats, Language.Java, codeLines, commentLines, blankLines);
    }

    private void verifyStats(final Map<Language, Stats> languageStats, final Language language,
                             final int codeLines, final int commentLines, final int blankLines) {
        final Stats stats = languageStats.get(language);
        assertThat(stats).isNotNull();
        verifyStats(stats, codeLines, commentLines, blankLines);
    }

    private void verifyStats(final Stats stats, final int codeLines, final int commentLines, final int blankLines) {
        assertThat(stats.codeLines).isEqualTo(codeLines);
        assertThat(stats.commentLines).isEqualTo(commentLines);
        assertThat(stats.blankLines).isEqualTo(blankLines);
    }
}
