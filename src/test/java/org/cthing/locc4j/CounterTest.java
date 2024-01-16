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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
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

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("MethodOnlyUsedFromInnerClass")
public class CounterTest {

    private static long countingTime;

    private final Config config = new Config();
    private final Counter.State state = new Counter.State();
    private final FileStats fileStats = new FileStats(new File("/tmp/testing.txt"));

    @AfterAll
    public static void allDone() {
        System.out.println("Counting time: " + countingTime + " ms");
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
            assertThat(makeCounter(Language.Kotlin).parseMultiLineComment(data("/*"))).isEqualTo(2);
            assertThat(stack()).containsExactly("*/", "*/");
        }

        @Test
        @DisplayName("Found nested comment using dedicated nested comment syntax")
        public void testFoundNestedSyntaxNestable() {
            stack("*/");
            assertThat(makeCounter(Language.D).parseMultiLineComment(data("/+"))).isEqualTo(2);
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
            assertThat(makeCounter(Language.Python).parseQuote(data("\"\"\"\nHello world"))).isEqualTo(3);
            assertThat(quote()).isEqualTo("\"\"\"");
            assertThat(quoteType()).isEqualTo(Counter.QuoteType.DOC);
        }

        @Test
        @DisplayName("Found verbatim string")
        public void testFoundVerbatimString() {
            assertThat(makeCounter(Language.Rust).parseQuote(data("r##\"\nHello world"))).isEqualTo(4);
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
            CounterTest.this.config.setCountDocStrings(true);
            assertThat(makeCounter(Language.Python).isComment(data("hello"), true)).isTrue();
        }

        @Test
        @DisplayName("Not counting doc strings")
        public void testCase1B() {
            quote("*/");
            quoteType(Counter.QuoteType.DOC);
            CounterTest.this.config.setCountDocStrings(false);
            assertThat(makeCounter(Language.Python).isComment(data("hello"), true)).isFalse();
        }

        @Test
        @DisplayName("Not a doc string")
        public void testCase1C() {
            quote("*/");
            quoteType(Counter.QuoteType.NORMAL);
            CounterTest.this.config.setCountDocStrings(false);
            assertThat(makeCounter(Language.Python).isComment(data("hello"), true)).isFalse();
        }

        @Test
        @DisplayName("Last line of doc comments")
        public void testCase2() {
            assertThat(makeCounter(Language.Python).isComment(data("hello\"\"\""), true)).isTrue();

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
            verifyStats(0, 0, 0);
            assertThat(makeCounter().parseSingleLine(data("hello"), stats(Language.Java))).isFalse();
            verifyStats(0, 0, 0);
        }

        @Test
        @DisplayName("Blank line")
        public void testBlankLine() {
            verifyStats(0, 0, 0);
            assertThat(stats().blankLines).isZero();
            assertThat(makeCounter().parseSingleLine(data("  "), stats())).isTrue();
            verifyStats(0, 0, 1);
        }

        @Test
        @DisplayName("Multiline")
        public void testMultiline() {
            verifyStats(0, 0, 0);
            assertThat(makeCounter().parseSingleLine(data("/*"), stats())).isFalse();
            verifyStats(0, 0, 0);
        }

        @Test
        @DisplayName("Literate")
        public void testLiterate() {
            verifyStats(0, 0, 0);
            assertThat(makeCounter(Language.Text).parseSingleLine(data("Hello world"), stats())).isTrue();
            verifyStats(0, 1, 0);
        }

        @Test
        @DisplayName("Line comment")
        public void testLineComment() {
            verifyStats(0, 0, 0);
            assertThat(makeCounter().parseSingleLine(data("// Hello world"), stats())).isTrue();
            verifyStats(0, 1, 0);
        }

        @Test
        @DisplayName("Code")
        public void testCode() {
            verifyStats(0, 0, 0);
            assertThat(makeCounter().parseSingleLine(data("int foo = 1;"), stats())).isTrue();
            verifyStats(1, 0, 0);
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
                    makeCounter(Language.Html).performMultiLineAnalysis(data(content), 0, content.length(),
                                                                        CounterTest.this.fileStats);
            assertThat(embedded).isNull();
        }

        @Test
        public void testCase2() throws IOException {
            quote("*/");
            final String content = "   */";
            final Embedding.Embedded embedded =
                    makeCounter().performMultiLineAnalysis(data(content), 0, content.length(),
                                                           CounterTest.this.fileStats);
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
                    makeCounter(Language.Html).performMultiLineAnalysis(data(content), 0, content.length(),
                                                                        CounterTest.this.fileStats);
            assertThat(embedded).isNotNull();
            assertThat(embedded.getLanguage()).isEqualTo(Language.JavaScript);
            assertThat(embedded.getEmbeddedStart()).isEqualTo(0);
            assertThat(embedded.getCodeEnd()).isEqualTo(20);
            assertThat(embedded.getCommentLines()).isEqualTo(0);
            assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
            assertThat(embedded.getCode().toString()).isEqualTo("var i = 0;");
        }

        @Test
        public void testCase4A() throws IOException {
            final String content = "/*";
            final Embedding.Embedded embedded =
                    makeCounter().performMultiLineAnalysis(data(content), 0, content.length(),
                                                           CounterTest.this.fileStats);
            assertThat(embedded).isNull();
        }

        @Test
        public void testCase4B() throws IOException {
            final String content = "\"Hello";
            final Embedding.Embedded embedded =
                    makeCounter().performMultiLineAnalysis(data(content), 0, content.length(),
                                                           CounterTest.this.fileStats);
            assertThat(embedded).isNull();
        }

        @Test
        public void testCase5() throws IOException {
            final String content = "// Hello";
            final Embedding.Embedded embedded =
                    makeCounter().performMultiLineAnalysis(data(content), 0, content.length(),
                                                           CounterTest.this.fileStats);
            assertThat(embedded).isNull();
        }

        @Test
        public void testCase6() throws IOException {
            final String content = """
                                   <p>Hello World</p>
                                   """;
            final Embedding.Embedded embedded =
                    makeCounter(Language.Html).performMultiLineAnalysis(data(content), 0, content.length(),
                                                                        CounterTest.this.fileStats);
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
        final char[] data =
                IOUtils.toCharArray(Objects.requireNonNull(getClass().getResourceAsStream("/data/" + filename)),
                                    StandardCharsets.UTF_8);

        final FileStats actualFileStats = new FileStats(new File(filename));
        this.config.setCountDocStrings(accessor.getBoolean(1));

        final long startMillis = System.currentTimeMillis();
        final Counter counter = makeCounter(accessor.get(2, Language.class));
        counter.count(data, actualFileStats);
        countingTime += System.currentTimeMillis() - startMillis;

        final Map<Language, LanguageStats> actualStatsMap = actualFileStats.getStats();
        assertThat(actualStatsMap).as("Incorrect number of languages counted").hasSize(numLanguageParams / 4);

        for (int i = 0; i < numLanguageParams; i += 4) {
            final Language language = accessor.get(i + 2, Language.class);
            final int codeLines = accessor.getInteger(i + 3);
            final int commentLines = accessor.getInteger(i + 4);
            final int blankLines = accessor.getInteger(i + 5);
            assertThat(actualStatsMap).hasEntrySatisfying(language, languageStats -> {
                assertThat(languageStats.codeLines).as(language + ": Code lines").isEqualTo(codeLines);
                assertThat(languageStats.commentLines).as(language + ": Comment lines").isEqualTo(commentLines);
                assertThat(languageStats.blankLines).as(language + ": Blank lines").isEqualTo(blankLines);
            });
        }
    }

    private Counter makeCounter() {
        return makeCounter(Language.Java);
    }

    private Counter makeCounter(final Language language) {
        return new Counter(language, this.config, this.state);
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

    private LanguageStats stats() {
        return stats(Language.Java);
    }

    private LanguageStats stats(final Language language) {
        return this.fileStats.stats(language);
    }

    private void verifyStats(final int codeLines, final int commentLines, final int blankLines) {
        verifyStats(Language.Java, codeLines, commentLines, blankLines);
    }

    private void verifyStats(final Language language, final int codeLines, final int commentLines,
                             final int blankLines) {
        final LanguageStats stats = stats(language);
        assertThat(stats.codeLines).isEqualTo(codeLines);
        assertThat(stats.commentLines).isEqualTo(commentLines);
        assertThat(stats.blankLines).isEqualTo(blankLines);
    }
}
