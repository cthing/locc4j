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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.cthing.annotations.AccessForTesting;


/**
 * Information about each language that can be counted.
 */
public enum Language {
<#list languages as id, entry>
    ${id}(
        "<#if entry.name()?has_content>${entry.name()}<#else>${id}</#if>",
        ${entry.literate()?c},
        List.of(<@expand_params params=entry.lineComments()/>),
        List.of(<@expand_block_params params=entry.multiLineComments()/>),
        ${entry.nested()?c},
        List.of(<@expand_block_params params=entry.nestedComments()/>),
        List.of(<@expand_block_params params=entry.quotes()/>),
        List.of(<@expand_block_params params=entry.verbatimQuotes()/>),
        List.of(<@expand_block_params params=entry.docQuotes()/>),
        ${entry.columnSignificant()?c},
        List.of(<@expand_params params=entry.importantSyntax()/>),
        List.of(<@expand_params params=entry.extensions()/>)
    )<#if id?is_last>;<#else>,</#if>
</#list>

    private static final Map<String, Language> EXTENSIONS = new HashMap<>();
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final String ENV_SHEBANG = "#!/usr/bin/env";

    private final String name;
    private final boolean literate;
    private final List<String> lineComments;
    private final List<BlockDelimiter> multiLineComments;
    private final boolean nestable;
    private final List<BlockDelimiter> nestedComments;
    private final List<BlockDelimiter> quotes;
    private final List<BlockDelimiter> verbatimQuotes;
    private final List<BlockDelimiter> docQuotes;
    private final boolean columnSignificant;
    private final List<String> importantSyntax;
    private final List<String> extensions;

    static {
        for (final Language language : values()) {
            language.extensions.forEach(ext -> EXTENSIONS.put(ext, language));
        }
    }

    Language(final String name, final boolean literate, final List<String> lineComments,
             final List<BlockDelimiter> multiLineComments, final boolean nestable,
             final List<BlockDelimiter> nestedComments, final List<BlockDelimiter> quotes,
             final List<BlockDelimiter> verbartimQuotes, final List<BlockDelimiter> docQuotes,
             final boolean columnSignificant, final List<String> importantSyntax,
             final List<String> extensions) {
        this.name = name;
        this.literate = literate;
        this.lineComments = lineComments;
        this.multiLineComments = multiLineComments;
        this.nestable = nestable;
        this.nestedComments = nestedComments;
        this.quotes = quotes;
        this.verbatimQuotes = verbartimQuotes;
        this.docQuotes = docQuotes;
        this.columnSignificant = columnSignificant;
        this.importantSyntax = importantSyntax;
        this.extensions = extensions;
    }

    public String getName() {
        return this.name;
    }

    public boolean isLiterate() {
        return this.literate;
    }

    public List<String> getLineComments() {
        return this.lineComments;
    }

    public List<BlockDelimiter> getMultiLineComments() {
        return this.multiLineComments;
    }

    public boolean isNestable() {
        return this.nestable;
    }

    public List<BlockDelimiter> getNestedComments() {
        return this.nestedComments;
    }

    public List<BlockDelimiter> getQuotes() {
        return this.quotes;
    }

    public List<BlockDelimiter> getVerbatimQuotes() {
        return this.verbatimQuotes;
    }

    public List<BlockDelimiter> getDocQuotes() {
        return this.docQuotes;
    }

    public boolean isColumnSignificant() {
        return this.columnSignificant;
    }

    public List<String> getImportantSyntax() {
        return this.importantSyntax;
    }

    public List<String> getExtensions() {
        return this.extensions;
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
     * @return Language corresponding to the specified file extension, if found.
     */
    @AccessForTesting
    static Optional<Language> fromFileExtension(final String extension) {
        return Optional.ofNullable(EXTENSIONS.get(extension));
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

        final String[] words = WHITESPACE.split(firstLine);
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
