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

package org.cthing.locc4j.plugins;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.text.StringEscapeUtils;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Represents an entry for a language in the languages.json file.
 *
 * @param name Common name for the language. This may differ from the language enum if the command name has spaces
 *      or contains characters not allowed in an enum (e.g. C++, C#, C Shell).
 * @param description Description of the language
 * @param see URL of the home page for the language
 * @param lineComments Character sequences that indicate a comment that spans a single line
 * @param multiLineComments Pairs of character sequences that indicate the start and end of comments that can span
 *      multiple lines
 * @param extensions Common file extensions for the language (without the leading period). Extensions are specified
 *      as lowercase, regardless of whether it is typically written with capital letters. Extension matching is
 *      case-insensitive.
 * @param quotes Pairs of character sequences that indicate the start and end of quoted text
 * @param verbatimQuotes Pairs of character sequences that indicate the start and end of a section of text that
 *      requires no escaping of special characters such as line feeds
 * @param docQuotes Pairs of character sequences that indicate the start and end of a documentation string
 * @param shebangs Strings that identify the language based on {@code #!} line at the start of a file
 * @param nested Indicates whether multiline comments can be nested
 * @param env Strings that identify the language based on the parameter to the {@code #!/usr/bin/env} line at the
 *      start of a file
 * @param literate Indicates whether the language is considered to primarily be documentation and is counted primarily
 *      as comments rather than procedural code
 * @param nestedComments Pairs of character sequences that indicate the start and end of nested multiline comments
 * @param mime The MIME types associated with the languages
 * @param columnSignificant Indicates whether the column position of a character sequence must be preserved because
 *      it is significant to the language. For example, in legacy FORTRAN a "C" in the first column indicates
 *      a line comment.
 * @param importantSyntax Opening quotes and the start of block comments are examples of syntax that indicates the
 *      portions of a file that can be trivially parsed and those sections that require more complex parsing (e.g.
 *      within a block comment).
 * @param filenames Certain languages have one or more standard filenames that do not have a file extension
 *      (e.g. Makefile, Dockerfile). Filenames are specified in lowercase, regardless of whether the filename
 *      is typically written with capital letters. Filename matching is case-insensitive. Filenames take precedence
 *      over extensions. For example, a file named {@code CMakeLists.txt} is detected as a CMake file, not a Text file.
 * @param embedSyntax If the language allows embedding other languages within it, this field indicates the syntax
 *      family it uses. Currently, the available syntax families are {@code html}, {@code markdown}, and {@code rust}.
 *      For example, the {@code html} syntax family uses HTML embedding tags (e.g. {@literal <script>},
 *      {@literal <style>}, and {@literal <template>}).
 */
public record LanguageEntry(
        @JsonProperty("name") @Nullable String name,
        @JsonProperty("description") @Nullable String description,
        @JsonProperty("see") @Nullable String see,
        @JsonProperty("line_comment") @Nullable List<String> lineComments,
        @JsonProperty("multi_line_comments") @Nullable List<List<String>> multiLineComments,
        @JsonProperty("extensions") @Nullable List<String> extensions,
        @JsonProperty("quotes") @Nullable List<List<String>> quotes,
        @JsonProperty("verbatim_quotes") @Nullable List<List<String>> verbatimQuotes,
        @JsonProperty("doc_quotes") @Nullable List<List<String>> docQuotes,
        @JsonProperty("shebangs") @Nullable List<String> shebangs,
        @JsonProperty("nested") boolean nested,
        @JsonProperty("env") @Nullable List<String> env,
        @JsonProperty("literate") boolean literate,
        @JsonProperty("nested_comments") @Nullable List<List<String>> nestedComments,
        @JsonProperty("mime") @Nullable List<String> mime,
        @JsonProperty("column_significant") boolean columnSignificant,
        @JsonProperty("important_syntax") @Nullable List<String> importantSyntax,
        @JsonProperty("filenames") @Nullable List<String> filenames,
        @JsonProperty("embed_syntax") @Nullable String embedSyntax
) {
    private static final Pattern REGEX_ESCAPE_PATTERN = Pattern.compile("([\\^$.|?*+()\\[\\]{}])");
    private static final Pattern REGEX_CHAR_CLASS_ESCAPE_PATTERN = Pattern.compile("([\\^\\[\\]])");

    @Override
    @Nullable
    public String description() {
        if (this.description == null) {
            return null;
        }
        return this.description.endsWith(".") ? this.description : (this.description + ".");
    }

    @Override
    public List<String> lineComments() {
        return this.lineComments == null ? List.of() : escapeJavaList(this.lineComments);
    }

    @Override
    public List<List<String>> multiLineComments() {
        return this.multiLineComments == null ? List.of() : escapeJavaListList(this.multiLineComments);
    }

    @Override
    public List<String> extensions() {
        return this.extensions == null ? List.of() : escapeJavaList(this.extensions);
    }

    @Override
    public List<List<String>> quotes() {
        return this.quotes == null ? List.of() : escapeJavaListList(this.quotes);
    }

    @Override
    public List<List<String>> verbatimQuotes() {
        return this.verbatimQuotes == null ? List.of() : escapeJavaListList(this.verbatimQuotes);
    }

    @Override
    public List<List<String>> docQuotes() {
        return this.docQuotes == null ? List.of() : escapeJavaListList(this.docQuotes);
    }

    @Override
    public List<String> shebangs() {
        return this.shebangs == null ? List.of() : this.shebangs;
    }

    @Override
    public List<String> env() {
        return this.env == null ? List.of() : this.env;
    }

    @Override
    public List<List<String>> nestedComments() {
        return this.nestedComments == null ? List.of() : escapeJavaListList(this.nestedComments);
    }

    @Override
    public List<String> mime() {
        return this.mime == null ? List.of() : this.mime;
    }

    /**
     * Creates a regular expression that combines the start delimiters of quotes, document quotes, multiline comments
     * and nested comments, along with any additional important syntax. This regex is used to find these sequences
     * in the text being counted. These character sequences indicate the type of parsing required. For example,
     * more complex parsing is required within a block comment than within a plain line of code.
     *
     * @return Regular expression representing important start delimiters and other syntax.
     */
    public String importantSyntaxRegex() {
        Stream<String> syntaxStream = quotes().stream().map(pair -> pair.get(0));
        syntaxStream = Stream.concat(syntaxStream, docQuotes().stream().map(pair -> pair.get(0)));
        syntaxStream = Stream.concat(syntaxStream, multiLineComments().stream().map(pair -> pair.get(0)));
        syntaxStream = Stream.concat(syntaxStream, nestedComments().stream().map(pair -> pair.get(0)));
        if (this.importantSyntax != null) {
            syntaxStream = Stream.concat(syntaxStream, this.importantSyntax.stream());
        }

        final List<String> important = syntaxStream.toList();
        if (important.isEmpty()) {
            return "null";
        }
        if (isCharacterClass(important)) {
            if (important.size() == 1) {
                return "compile(\"" + escapeRegexChracterClass(important).get(0) + "\")";
            }
            return "compile(\"[" + String.join("", escapeRegexChracterClass(important)) + "]\")";
        }
        return "compile(\"" + String.join("|", escapeRegexList(important)) + "\")";
    }

    /**
     * Creates a list of all multiline comment character sequences, both normal and nested.
     *
     * @return All multiline comment character sequences.
     */
    public List<List<String>> allMultiLineComments() {
        final Stream<List<String>> multiLineStream = multiLineComments().stream();
        final Stream<List<String>> nestedStream = nestedComments().stream();
        return Stream.concat(multiLineStream, nestedStream).collect(Collectors.toList());
    }

    @Override
    public List<String> filenames() {
        return this.filenames == null ? List.of() : this.filenames;
    }

    /**
     * Determines whether the specified list of regular expressions can be combined into a character class rather
     * than alternations.
     *
     * @param regexes Regular expressions to test
     * @return {@code true} if the specified regular expressions can be combined into a character class.
     */
    private static boolean isCharacterClass(final List<String> regexes) {
        return regexes.stream().allMatch(regex -> regex.length() == 1 || "\\\"".equals(regex));
    }

    /**
     * Performs escaping of the specified list of lists of strings so that they can appear in Java source code.
     *
     * @param stringsList List of lists of strings to escape
     * @return Escaped list of lists of strings
     */
    private static List<List<String>> escapeJavaListList(final List<List<String>> stringsList) {
        return stringsList.stream().map(LanguageEntry::escapeJavaList).toList();
    }

    /**
     * Performs escaping of the specified list of strings so that they can appear in Java source code.
     *
     * @param strings List of strings to escape
     * @return Escaped list of strings
     */
    private static List<String> escapeJavaList(final List<String> strings) {
        return strings.stream().map(StringEscapeUtils::escapeJava).toList();
    }

    /**
     * Performs escaping on the specified list of regular expressions.
     *
     * @param regexes List of regular expressions to escape
     * @return List of escaped regular expressions.
     */
    private static List<String> escapeRegexList(final List<String> regexes) {
        return regexes.stream()
                      .map(regex -> REGEX_ESCAPE_PATTERN.matcher(regex).replaceAll("\\\\\\\\$1"))
                      .collect(Collectors.toList());
    }

    /**
     * Performs escaping on the specified list of regular expressions for use in a character class.
     *
     * @param regexes List of regular expressions to escape
     * @return List of escaped regular expressions.
     */
    private static List<String> escapeRegexChracterClass(final List<String> regexes) {
        return regexes.stream()
                      .map(regex -> REGEX_CHAR_CLASS_ESCAPE_PATTERN.matcher(regex).replaceAll("\\\\\\\\$1"))
                      .collect(Collectors.toList());
    }
}
