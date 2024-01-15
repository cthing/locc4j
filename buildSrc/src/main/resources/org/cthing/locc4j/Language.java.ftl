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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.cthing.annotations.AccessForTesting;

import static java.util.regex.Pattern.compile;

import static org.cthing.locc4j.RegexUtils.WHITESPACE_REGEX;
import static org.cthing.locc4j.RegexUtils.NOTHING_REGEX;

/**
 * Information about each language that can be counted.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "MethodDoesntCallSuperMethod", "Convert2streamapi", "ForLoopReplaceableByForEach"})
public enum Language {
<#list languages as id, entry>
    ${id}(
        "<#if entry.name()?has_content>${entry.name()}<#else>${id}</#if>",
        ${entry.literate()?c},
        ${entry.nested()?c},
        new BlockDelimiter[] {<@expand_block_params params=entry.nestedComments()/>},
        new BlockDelimiter[] {<@expand_block_params params=entry.quotes()/>},
        new BlockDelimiter[] {<@expand_block_params params=entry.verbatimQuotes()/>},
        new BlockDelimiter[] {<@expand_block_params params=entry.docQuotes()/>},
        ${entry.columnSignificant()?c},
        ${entry.importantSyntaxRegex()},
        new String[] {<@expand_params params=entry.extensions()/>},
        new BlockDelimiter[] {<@expand_block_params params=entry.allMultiLineComments()/>}
    ) {
        <#if entry.lineComments()?has_content>
        @Override
        public boolean isLineComment(final Predicate<CharSequence> predicate) {
            return <#list entry.lineComments() as comment>predicate.test("${comment}")<#if comment?has_next> || </#if></#list>;
        }
        </#if>

        <#if entry.embedSyntax()?has_content>
        @Override
        @Nullable
        public Embedding.Syntax getEmbedSyntax() {
            return Embedding.Syntax.${entry.embedSyntax()?lower_case};
        }
        </#if>
    }<#if id?is_last>;<#else>,</#if>
</#list>

    private static final Map<String, Language> NAMES = new HashMap<>();
    private static final Map<String, Language> EXTENSIONS = new HashMap<>();
    private static final String ENV_SHEBANG = "#!/usr/bin/env";

    private final String name;
    private final boolean literate;
    private final boolean nestable;
    private final BlockDelimiter[] nestedComments;
    private final BlockDelimiter[] quotes;
    private final BlockDelimiter[] verbatimQuotes;
    private final BlockDelimiter[] docQuotes;
    private final boolean columnSignificant;
    private final Pattern importantSyntax;
    private final String[] extensions;
    private final BlockDelimiter[] allMultiLineComments;

    static {
        for (final Language language : values()) {
            NAMES.put(language.getName().toLowerCase(Locale.ROOT), language);
            for (int i = 0; i < language.extensions.length; i++) {
                EXTENSIONS.put(language.extensions[i], language);
            }
        }
    }

    Language(final String name, final boolean literate, final boolean nestable,
             final BlockDelimiter[] nestedComments, final BlockDelimiter[] quotes,
             final BlockDelimiter[] verbartimQuotes, final BlockDelimiter[] docQuotes,
             final boolean columnSignificant, final Pattern importantSyntax,
             final String[] extensions, final BlockDelimiter[] allMultiLineComments) {
        this.name = name;
        this.literate = literate;
        this.nestable = nestable;
        this.nestedComments = nestedComments;
        this.quotes = quotes;
        this.verbatimQuotes = verbartimQuotes;
        this.docQuotes = docQuotes;
        this.columnSignificant = columnSignificant;
        this.importantSyntax = importantSyntax;
        this.extensions = extensions;
        this.allMultiLineComments = allMultiLineComments;
    }

    public String getName() {
        return this.name;
    }

    public boolean isLiterate() {
        return this.literate;
    }

    public boolean isLineComment(final Predicate<CharSequence> predicate) {
        return false;
    }

    public boolean isNestable() {
        return this.nestable;
    }

    public boolean isNestedComment(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.nestedComments.length; i++) {
            if (predicate.test(this.nestedComments[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean isQuote(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.quotes.length; i++) {
            if (predicate.test(this.quotes[i])) {
                return true;
            }
        }
        return false;
    }

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

    public boolean isVerbatimQuote(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.verbatimQuotes.length; i++) {
            if (predicate.test(this.verbatimQuotes[i])) {
                return true;
            }
        }
        return false;
    }

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

    public boolean isDocQuote(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.docQuotes.length; i++) {
            if (predicate.test(this.docQuotes[i])) {
                return true;
            }
        }
        return false;
    }

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

    public boolean isColumnSignificant() {
        return this.columnSignificant;
    }

    public Pattern getImportantSyntax() {
        return this.importantSyntax;
    }

    @Nullable
    public Embedding.Syntax getEmbedSyntax() {
        return null;
    }

    public boolean isAnyMultiLineComment(final Predicate<BlockDelimiter> predicate) {
        for (int i = 0; i < this.allMultiLineComments.length; i++) {
            if (predicate.test(this.allMultiLineComments[i])) {
                return true;
            }
        }
        return false;
    }

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
    public static Optional<Language> fromFile(final File file) {
        final String filename = file.getName().toLowerCase(Locale.ROOT);

        switch (filename) {
<#list languages as id, entry><#list entry.filenames() as filename>
<#if filename?is_first>            case "${filename}"<#else>, "${filename}"</#if><#if filename?is_last>: return Optional.of(${id});
</#if>
</#list></#list>
            default: break;
        }

        final String extension = FilenameUtils.getExtension(filename);
        return extension.isEmpty() ? Optional.empty() : fromFileExtension(extension).or(() -> fromShebang(file));
    }

    /**
     * Attempts to obtain the language of a file based on its MIME type.
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
     *
     * @param file File whose shebang is to be matched
     * @return Language corresponding to the specified file's shebang, if found.
     */
    @AccessForTesting
    static Optional<Language> fromShebang(final File file) {
        final String firstLine;

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
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
