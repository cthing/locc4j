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

import java.util.List;

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
        List.of(<@expand_params params=entry.shebangs()/>),
        ${entry.columnSignificant()?c},
        List.of(<@expand_params params=entry.importantSyntax()/>)
    )<#if id?is_last>;<#else>,</#if>
</#list>

    private final String name;
    private final boolean literate;
    private final List<String> lineComments;
    private final List<BlockDelimiter> multiLineComments;
    private final boolean nestable;
    private final List<BlockDelimiter> nestedComments;
    private final List<BlockDelimiter> quotes;
    private final List<BlockDelimiter> verbatimQuotes;
    private final List<BlockDelimiter> docQuotes;
    private final List<String> shebangs;
    private final boolean columnSignificant;
    private final List<String> importantSyntax;

    Language(final String name, final boolean literate, final List<String> lineComments,
             final List<BlockDelimiter> multiLineComments, final boolean nestable,
             final List<BlockDelimiter> nestedComments, final List<BlockDelimiter> quotes,
             final List<BlockDelimiter> verbartimQuotes, final List<BlockDelimiter> docQuotes,
             final List<String> shebangs, final boolean columnSignificant,
             final List<String> importantSyntax) {
        this.name = name;
        this.literate = literate;
        this.lineComments = lineComments;
        this.multiLineComments = multiLineComments;
        this.nestable = nestable;
        this.nestedComments = nestedComments;
        this.quotes = quotes;
        this.verbatimQuotes = verbartimQuotes;
        this.docQuotes = docQuotes;
        this.shebangs = shebangs;
        this.columnSignificant = columnSignificant;
        this.importantSyntax = importantSyntax;
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

    public List<String> getShebangs() {
        return this.shebangs;
    }

    public boolean isColumnSignificant() {
        return this.columnSignificant;
    }

    public List<String> getImportantSyntax() {
        return this.importantSyntax;
    }
}
