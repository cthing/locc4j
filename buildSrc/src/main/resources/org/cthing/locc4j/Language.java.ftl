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

<#-- The following comment does not apply to this template file -->
// GENERATED FILE - DO NOT EDIT

package org.cthing.locc4j;

<#macro expand_params params>
    <#list params as param>"${param}"<#if param?has_next>, </#if></#list><#t>
</#macro>
<#macro expand_block_params params>
    <#list params as param>new BlockDelimiter("${param[0]}", "${param[1]}")<#if param?has_next>, </#if></#list><#t>
</#macro>

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;

import static java.util.regex.Pattern.compile;

/**
 * Languages that can be counted. Click on a language to see the list of file extensions and/or file names
 * used by the language.
 */
@SuppressWarnings({
        "OptionalUsedAsFieldOrParameterType", "MethodDoesntCallSuperMethod",
        "Convert2streamapi", "ForLoopReplaceableByForEach", "UnnecessaryUnicodeEscape",
        "RegExpDuplicateAlternationBranch", "RegExpRedundantEscape"
})
public enum Language {
<#list languages as id, entry>
    /**
     * <#if entry.description()?has_content>${entry.description()}<#else>Identifier for the <#if entry.name()?has_content>${entry.name()}<#else>${id}</#if> language.</#if>
     * <#if entry.extensions()?has_content><p>File extensions: <#list entry.extensions() as ext>{@code ${ext}}<#if ext?has_next>, </#if></#list></p></#if>
     * <#if entry.filenames()?has_content><p>File names: <#list entry.filenames() as filename>{@code ${filename}}<#if filename?has_next>, </#if></#list></p></#if>
     * <#if entry.see()?has_content>@see <a href="${entry.see()}">${entry.see()}</a></#if>
     */
    ${id}(
        "<#if entry.name()?has_content>${entry.name()}<#else>${id}</#if>",
        new BlockDelimiter[] {<@expand_block_params params=entry.nestedComments()/>},
        new BlockDelimiter[] {<@expand_block_params params=entry.quotes()/>},
        new BlockDelimiter[] {<@expand_block_params params=entry.verbatimQuotes()/>},
        new BlockDelimiter[] {<@expand_block_params params=entry.docQuotes()/>},
        ${entry.importantSyntaxRegex()},
        new String[] {<@expand_params params=entry.extensions()/>},
        new BlockDelimiter[] {<@expand_block_params params=entry.allMultiLineComments()/>}
    ) {
        <#if entry.literate()>
        @Override
        public boolean isLiterate() {
            return true;
        }

        </#if>
        <#if entry.nested()>
        @Override
        public boolean isNestable() {
            return true;
        }

        </#if>
        <#if entry.columnSignificant()>
        @Override
        public boolean isColumnSignificant() {
            return true;
        }

        </#if>
        <#if entry.lineComments()?has_content>
        @Override
        public boolean isLineComment(final Predicate<CharSequence> predicate) {
            return <#list entry.lineComments() as comment>predicate.test("${comment}")<#if comment?has_next> || </#if></#list>;
        }

        </#if>
        <#if entry.verbatimQuotes()?has_content>
        @Override
        public boolean isVerbatimQuote(final Predicate<BlockDelimiter> predicate) {
            for (int i = 0; i < this.verbatimQuotes.length; i++) {
                if (predicate.test(this.verbatimQuotes[i])) {
                    return true;
                }
            }
            return false;
        }

        </#if>
        <#if entry.nestedComments()?has_content>
        @Override
        public boolean isNestedComment(final Predicate<BlockDelimiter> predicate) {
            for (int i = 0; i < this.nestedComments.length; i++) {
                if (predicate.test(this.nestedComments[i])) {
                    return true;
                }
            }
            return false;
        }

        </#if>
        <#if entry.embedSyntax()?has_content>
        @Override
        public Embedding.Syntax getEmbedSyntax() {
            return Embedding.Syntax.${entry.embedSyntax()?lower_case};
        }
        </#if>
    }<#if id?is_last>;<#else>,</#if>
</#list>

    private static final Map<String, Language> NAMES = new HashMap<>();
    private static final Map<String, Language> EXTENSIONS = new HashMap<>();
    private static final String ENV_SHEBANG = "#!/usr/bin/env";
    private static final Pattern WHITESPACE_REGEX = compile("\\s+");

    final BlockDelimiter[] nestedComments;
    final BlockDelimiter[] verbatimQuotes;
    @Nullable
    final Pattern importantSyntax;

    private final String name;
    private final BlockDelimiter[] quotes;
    private final BlockDelimiter[] docQuotes;
    private final String[] extensions;
    private final BlockDelimiter[] allMultiLineComments;

    static {
        // Static maps must be used to avoid generating methods that exceed the JVM method byte code limit.
        for (final Language language : values()) {
            NAMES.put(language.getName().toLowerCase(Locale.ROOT), language);
        }
        resetExtensions();
    }

    Language(final String name, final BlockDelimiter[] nestedComments, final BlockDelimiter[] quotes,
             final BlockDelimiter[] verbartimQuotes, final BlockDelimiter[] docQuotes,
             @Nullable final Pattern importantSyntax,
             final String[] extensions, final BlockDelimiter[] allMultiLineComments) {
        this.name = name;
        this.nestedComments = nestedComments;
        this.quotes = quotes;
        this.verbatimQuotes = verbartimQuotes;
        this.docQuotes = docQuotes;
        this.importantSyntax = importantSyntax;
        this.extensions = extensions;
        this.allMultiLineComments = allMultiLineComments;
    }

    /**
     * Obtains the common name for the language. This may differ from the language enum if the command
     * name has spaces or contains characters not allowed in an enum (e.g. C++, C#, C Shell).
     *
     * @return Common name for the language
     */
    public String getName() {
        return this.name;
    }

    /**
     * Adds the specified file extension to specified language's list of extensions. If an extension already
     * maps to a language, it is replaced.
     *
     * @param extension File extension to add (without the leading period). Extensions are case-insensitive.
     * @param language Language to map to the specified extension
     */
    public static void addExtension(final String extension, final Language language) {
        EXTENSIONS.put(extension.toLowerCase(Locale.ROOT), language);
    }

    /**
     * Removes the specified file extension. If the extension is not present, this method does nothing.
     *
     * @param extension File extension to remove (without the leading period). Extensions are case-insensitive.
     */
    public static void removeExtension(final String extension) {
        EXTENSIONS.remove(extension);
    }

    /**
     * Obtains a read-only map of file extensions to languages.
     *
     * @return Read-only map of file extensions to languages.
     */
    public static Map<String, Language> getExtensions() {
        return Collections.unmodifiableMap(EXTENSIONS);
    }

    /**
     * Restores the file extension to language mapping to its default.
     */
    public static void resetExtensions() {
        for (final Language language : values()) {
            for (int i = 0; i < language.extensions.length; i++) {
                EXTENSIONS.put(language.extensions[i], language);
            }
        }
    }

    /**
     * Indicates whether the language is considered to be primarily documentation and is counted primarily
     * as comments rather than procedural code.
     *
     * @return {@code true} if the language is considered documentation.
     */
    public boolean isLiterate() {
        return false;
    }

    /**
     * Indicates whether the specified predicate is true for line comments.
     *
     * @param predicate Tests for line comments.
     * @return {@code true} if the predicate is true for line comments.
     */
    public boolean isLineComment(final Predicate<CharSequence> predicate) {
        return false;
    }

    /**
     * Indicates whether multiline comments can be nested.
     *
     * @return {@code true} if the language allows nesting of comments.
     */
    public boolean isNestable() {
        return false;
    }

    /**
     * Indicates whether the specified predicate is true for nested comments.
     *
     * @param predicate Tests for nested comments.
     * @return {@code true} if the predicate is true for nested comments.
     */
    public boolean isNestedComment(final Predicate<BlockDelimiter> predicate) {
        return false;
    }

    /**
     * Indicates whether the specified predicate is true for normal quotes.
     *
     * @param predicate Tests for normal quotes.
     * @return {@code true} if the predicate is true for normal quotes.
     */
    public boolean isQuote(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.quotes.length; i++) {
            if (predicate.test(this.quotes[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Provides the normal quote for which the specified predicate is true.
     *
     * @param predicate Tests for normal quotes.
     * @return Normal quote for which the specified predicate is true.
     */
    @Nullable
    public BlockDelimiter findQuote(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.quotes.length; i++) {
            final BlockDelimiter delim = this.quotes[i];
            if (predicate.test(delim)) {
                return delim;
            }
        }
        return null;
    }

    /**
     * Indicates whether the specified predicate is true for verbatim quotes.
     *
     * @param predicate Tests for verbatim quotes.
     * @return {@code true} if the predicate is true for verbatim quotes.
     */
    public boolean isVerbatimQuote(final Predicate<BlockDelimiter> predicate) {
        return false;
    }

    /**
     * Provides the verbatim quote for which the specified predicate is true.
     *
     * @param predicate Tests for verbatim quotes.
     * @return Verbatim quote for which the specified predicate is true.
     */
    @Nullable
    public BlockDelimiter findVerbatimQuote(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.verbatimQuotes.length; i++) {
            final BlockDelimiter delim = this.verbatimQuotes[i];
            if (predicate.test(delim)) {
                return delim;
            }
        }
        return null;
    }

    /**
     * Indicates whether the specified predicate is true for documentation quotes.
     *
     * @param predicate Tests for documentation quotes.
     * @return {@code true} if the predicate is true for documentation quotes.
     */
    public boolean isDocQuote(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.docQuotes.length; i++) {
            if (predicate.test(this.docQuotes[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Provides the documentation quote for which the specified predicate is true.
     *
     * @param predicate Tests for documentation quotes.
     * @return Documentation quote for which the specified predicate is true.
     */
    @Nullable
    public BlockDelimiter findDocQuote(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.docQuotes.length; i++) {
            final BlockDelimiter delim = this.docQuotes[i];
            if (predicate.test(delim)) {
                return delim;
            }
        }
        return null;
    }

    /**
     * Indicates whether the column position of a character sequence must be preserved because it
     * is significant to the language. For example, in legacy FORTRAN a "C" in the first column
     * indicates a line comment.
     *
     * @return {@code true} if the column position of a character is significant.
     */
    public boolean isColumnSignificant() {
        return false;
    }

    /**
     * Obtains a regular expression to match against syntax that is considered important for counting.
     *
     * @return Regular expression to match against important syntax.
     */
    @Nullable
    public Pattern getImportantSyntax() {
        return this.importantSyntax;
    }

    /**
     * Obtains the syntax for embedding other languages within this language.
     *
     * @return Syntax for embedding other languages.
     */
    @Nullable
    public Embedding.Syntax getEmbedSyntax() {
        return null;
    }

    /**
     * Indicates whether the specified predicate is true for any multiline comments.
     *
     * @param predicate Tests for multiline comments.
     * @return {@code true} if the predicate is true for multiline comments.
     */
    public boolean isAnyMultiLineComment(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.allMultiLineComments.length; i++) {
            if (predicate.test(this.allMultiLineComments[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Provides the multiline comment delimiter for which the specified predicate is true.
     *
     * @param predicate Tests for the multiline comment delimiter.
     * @return Multiline comment delimiter for which the specified predicate is true.
     */
    @Nullable
    public BlockDelimiter findAnyMultiLineComment(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.allMultiLineComments.length; i++) {
            final BlockDelimiter delim = this.allMultiLineComments[i];
            if (predicate.test(delim)) {
                return delim;
            }
        }
        return null;
    }

    /**
      * Attempts to obtain the language of a file by attempting to match the following:
      * <ol>
      *     <li>The name of the file against any filenames for a language (e.g. Makefile)</li>
      *     <li>The extension of the file against any filename extensions for a language (e.g. cpp)</li>
      *     <li>The shebang in the first line of the file against any shebang interpreters or {@code env}
      *         programs for a language</li>
      * </ol>
      *
      * @param file File whose language is to be determined
      * @return Language corresponding to the specified file. If the file cannot be found or read, or the language
      *     cannot be determined, an empty {@link Optional} is returned.
      */
    public static Optional<Language> fromFile(final Path file) {
        if (Files.isDirectory(file)) {
            throw new IllegalArgumentException("Path must be a file");
        }

        final Path filenamePath = file.getFileName();
        if (filenamePath == null) {
            throw new IllegalArgumentException("Path is empty");
        }
        final String filename = filenamePath.toString().toLowerCase(Locale.ROOT);

        switch (filename) {
<#list languages as id, entry><#list entry.filenames() as filename>
<#if filename?is_first>            case "${filename}"<#else>, "${filename}"</#if><#if filename?is_last>: return Optional.of(${id});
</#if>
</#list></#list>
            default: break;
        }

        final String extension = FilenameUtils.getExtension(filename);
        return extension.isEmpty() ? fromShebang(file) : fromFileExtension(extension).or(() -> fromShebang(file));
    }

    /**
     * Attempts to obtain the language of a file based on its MIME type.
     * <p>
     * The supported MIME types and their corresponding language are:
     * </p>
     * <ul>
<#list languages as id, entry><#list entry.mime() as mime>
     *   <li>{@code ${mime?right_pad(35, " .")}} <a href="#${id}">${id}</a></li>
</#list></#list>
     * </ul>
     *
     * @param mimeType File MIME type
     * @return Language corresponding to the specified MIME type. If the language cannot be determined, an empty
     *      {@link Optional} is returned.
     */
    public static Optional<Language> fromMime(final String mimeType) {
        return switch (mimeType) {
<#list languages as id, entry><#list entry.mime() as mime>
<#if mime?is_first>            case "${mime}"<#else>, "${mime}"</#if><#if mime?is_last> -> Optional.of(${id});
</#if>
</#list></#list>
            default -> Optional.empty();
        };
    }

    /**
     * Attempts to obtain the language of a file based on its file extension.
     *
     * @param extension File extension to search (without the leading ".")
     * @return Language corresponding to the specified file extension. If the language cannot be determined, an
     *      empty {@link Optional} is returned. The matching is case-insensitive.
     */
    public static Optional<Language> fromFileExtension(final String extension) {
        return Optional.ofNullable(EXTENSIONS.get(extension.toLowerCase(Locale.ROOT)));
    }

    /**
    * Attempts to obtain the language corresponding to the specified language identifier.
    *
    * @param id Language identifier to find
    * @return Language corresponding to the specified identifier. Comparisons are case-insensitive. If the language
    *       cannot be determined, an empty {@link Optional} is returned.
    */
    public static Optional<Language> fromId(final String id) {
        final String normalizedId = id.toLowerCase(Locale.ROOT);
        for (final Language language : values()) {
            if (language.toString().toLowerCase(Locale.ROOT).equals(normalizedId)) {
                return Optional.of(language);
            }
        }
        return Optional.empty();
    }

    /**
     * Attempts to obtain the language corresponding to the specified language name.
     *
     * @param langName Name of the language to find
     * @return Language corresponding to the specified name. Comparisons are case-insensitive. If the language
     *      cannot be determined, an empty {@link Optional} is returned.
     */
    public static Optional<Language> fromName(final String langName) {
        return Optional.ofNullable(NAMES.get(langName.toLowerCase(Locale.ROOT)));
    }

    /**
     * Attempts to obtain the language of the specified file based on a shebang ("#!") on its first line.
     * The shebang is first compared against the shebang interpreters listed for each language. If an
     * interpreter is not matched, the {@code env} program argument is checked.
     * <p>
     * The supported interpreters and their corresponding language are:
     * </p>
     * <ul>
<#list languages as id, entry><#list entry.shebangs() as shebang>
     *   <li>{@code ${shebang?right_pad(25, " .")}} <a href="#${id}">${id}</a></li>
</#list></#list>
     * </ul>
     * <p>
     * The supported environment programs are:
     * </p>
     * <ul>
<#list languages as id, entry><#list entry.env() as env>
     *   <li>{@code ${env?right_pad(25, " .")}} <a href="#${id}">${id}</a></li>
</#list></#list>
     * </ul>
     *
     * @param file File whose shebang is to be matched
     * @return Language corresponding to the specified file's shebang, if found.
     */
    public static Optional<Language> fromShebang(final Path file) {
        if (Files.isDirectory(file)) {
            throw new IllegalArgumentException("Path must be a file");
        }

        final String firstLine;

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            firstLine = reader.readLine();
        } catch (final IOException ex) {
            return Optional.empty();
        }

        if (firstLine == null) {
            return Optional.empty();
        }

        final String[] words = WHITESPACE_REGEX.split(firstLine);
        if (words.length == 0) {
            return Optional.empty();
        }

        // First try looking for a shebang interpreter
        switch (words[0]) {
<#list languages as id, entry><#list entry.shebangs() as shebang>
<#if shebang?is_first>            case "${shebang}"<#else>, "${shebang}"</#if><#if shebang?is_last>: return Optional.of(${id});
</#if>
</#list></#list>
            default: break;
        }

        // Next try looking for a shebang env program
        if (words.length < 2 || !ENV_SHEBANG.equals(words[0])) {
            return Optional.empty();
        }

        return switch (words[1]) {
<#list languages as id, entry><#list entry.env() as env>
<#if env?is_first>            case "${env}"<#else>, "${env}"</#if><#if env?is_last> -> Optional.of(${id});
</#if>
</#list></#list>
            default -> Optional.empty();
        };
    }
}
