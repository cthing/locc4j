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
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Represents an entry for a language in the languages.json file.
 *
 * @param name Common name for the language. This may differ from the language enum if the command name has spaces
 *      or contains characters not allowed in an enum (e.g. C++, C#, C Shell).
 * @param lineComments Character sequences that indicate a comment that spans a single line
 * @param multiLineComments Pairs of character sequences that indicate the start and end of comments that can span
 *      multiple lines
 * @param extensions Common file extensions for the language (without the leading period). Extensions are specified
 *      as lowercase, regardless of whether the it is typically written with capital letters. Extension matching is
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
 * @param importantSyntax Opening quotes and the start of block comments are examples of syntax that indicates a
 *      large section of a file can be skipped while parsing. This parameter provides additional character sequences
 *      that indicate a section that can be skipped while parsing.
 * @param filenames Certain languages have one or more standard filenames that do not have a file extension
 *      (e.g. Makefile, Dockerfile). Filenames are specified in lowercase, regardless of whether the filename
 *      is typically written with capital letters. Filename matching is case-insensitive. Filenames take precedence
 *      over extensions. For example, a file named {@code CMakeLists.txt} is detected as a CMake file, not a Text file.
 */
public record LanguageEntry(
        @JsonProperty("name") @Nullable String name,
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
        @JsonProperty("filenames") @Nullable List<String> filenames
) {
    @Override
    public List<String> lineComments() {
        return this.lineComments == null ? List.of() : this.lineComments;
    }

    @Override
    public List<List<String>> multiLineComments() {
        return this.multiLineComments == null ? List.of() : this.multiLineComments;
    }

    @Override
    public List<String> extensions() {
        return this.extensions == null ? List.of() : this.extensions;
    }

    @Override
    public List<List<String>> quotes() {
        return this.quotes == null ? List.of() : this.quotes;
    }

    @Override
    public List<List<String>> verbatimQuotes() {
        return this.verbatimQuotes == null ? List.of() : this.verbatimQuotes;
    }

    @Override
    public List<List<String>> docQuotes() {
        return this.docQuotes == null ? List.of() : this.docQuotes;
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
        return this.nestedComments == null ? List.of() : this.nestedComments;
    }

    @Override
    public List<String> mime() {
        return this.mime == null ? List.of() : this.mime;
    }

    @Override
    public List<String> importantSyntax() {
        final List<String> important = quotes().stream().map(pair -> pair.get(0)).collect(Collectors.toList());
        docQuotes().stream().map(pair -> pair.get(0)).forEach(important::add);
        multiLineComments().stream().map(pair -> pair.get(0)).forEach(important::add);
        nestedComments().stream().map(pair -> pair.get(0)).forEach(important::add);

        if (this.importantSyntax != null) {
            important.addAll(this.importantSyntax);
        }

        return important;
    }

    @Override
    public List<String> filenames() {
        return this.filenames == null ? List.of() : this.filenames;
    }
}
