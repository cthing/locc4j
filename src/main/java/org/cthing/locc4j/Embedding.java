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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;


/**
 * Provides support for languages that can contain other languages (e.g. JavaScript in HTML).
 */
final class Embedding {

    static final Pattern HTML_SCRIPT_START_REGEX = Pattern.compile("<script(?:.*type=\"(.*)\")?.*?>");
    static final Pattern HTML_SCRIPT_END_REGEX = Pattern.compile("</script>");

    static final Pattern HTML_STYLE_START_REGEX = Pattern.compile("<style(?:.*(?:lang|type)=\"(.*)\")?.*?>");
    static final Pattern HTML_STYLE_END_REGEX = Pattern.compile("</style>");

    static final Pattern HTML_TEMPLATE_START_REGEX = Pattern.compile("<template(?:.*lang=\"(.*)\")?.*?>");
    static final Pattern HTML_TEMPLATE_END_REGEX = Pattern.compile("</template>");

    static final Pattern HTML_SVG_START_REGEX = Pattern.compile("<svg.*>");
    static final Pattern HTML_SVG_END_REGEX = Pattern.compile("</svg>");

    static final Pattern LIQUID_SCHEMA_START_REGEX = Pattern.compile("\\{%\\s*schema\\s*%}");
    static final Pattern LIQUID_SCHEMA_END_REGEX = Pattern.compile("\\{%\\s*endschema\\s*%}");

    static final Pattern LIQUID_JAVASCRIPT_START_REGEX = Pattern.compile("\\{%\\s*javascript\\s*%}");
    static final Pattern LIQUID_JAVASCRIPT_END_REGEX = Pattern.compile("\\{%\\s*endjavascript\\s*%}");

    static final Pattern LIQUID_STYLESHEET_START_REGEX = Pattern.compile("\\{%\\s*stylesheet\\s*%}");
    static final Pattern LIQUID_STYLESHEET_END_REGEX = Pattern.compile("\\{%\\s*endstylesheet\\s*%}");

    static final String MARKDOWN_BLOCK_DELIM_1 = "```";
    static final String MARKDOWN_BLOCK_DELIM_2 = "~~~";
    static final Pattern MARKDOWN_CODE_START_REGEX =
            Pattern.compile("(" + MARKDOWN_BLOCK_DELIM_1 + "|" + MARKDOWN_BLOCK_DELIM_2 + ")(\\S+)\\s");
    static final Pattern MARKDOWN_CODE_END_1_REGEX = Pattern.compile(MARKDOWN_BLOCK_DELIM_1 + "\\s?");
    static final Pattern MARKDOWN_CODE_END_2_REGEX = Pattern.compile(MARKDOWN_BLOCK_DELIM_2 + "\\s?");

    static final String RUST_INNER_LINE_DOC = "//!";
    static final String RUST_OUTER_LINE_DOC = "///";

    static final Pattern COMMA_REGEX = Pattern.compile("\\s*,\\s*");

    /**
     * Among the languages that support embedding, there a set of common approaches. For example, Vue and Html
     * use the same tags for embedding.
     */
    enum Syntax {
        html,
        liquid,
        markdown,
        rust
    }


    /**
     * Represents content in a given language embedded within the content in a different language. For example,
     * JavaScript embedded within an HTML file.
     */
    interface Embedded {

        /**
         * Language of the embedded content.
         *
         * @return Language of the embedded content.
         */
        Language getLanguage();

        /**
         * Obtains the starting position of the embedding construct. For HTML, this is the start of the opening
         * tag for the embedded content (e.g. the {@literal <script>} tag).
         *
         * @return Starting position of the embedding construct.
         */
        int getEmbeddedStart();

        /**
         * Obtains the starting position of the embedded content. For HTML, this is the start of the embedded script,
         * style sheet or template.
         *
         * @return Position of the embedded content.
         */
        int getCodeEnd();

        /**
         * Obtains the number of additional code lines involved in the embedding. For HTML, this takes into account
         * the opening embedding tag. For Markdown, this takes into account the code fence.
         *
         * @return Additional code lines involved in the embedding.
         */
        int getAdditionalCodeLines();

        /**
         * Obtains the embedded content.
         *
         * @return Embedded content.
         */
        CharData getCode();
    }


    private abstract static class AbstractEmbedded implements Embedded {

        private final Language language;
        private final int start;
        private final int end;
        private final int additionalCodeLines;
        private final CharData code;

        protected AbstractEmbedded(final Language language, final int start, final int end,
                                   final int additionalCodeLines, final CharData code) {
            this.language = language;
            this.start = start;
            this.end = end;
            this.additionalCodeLines = additionalCodeLines;
            this.code = code;
        }

        @Override
        public Language getLanguage() {
            return this.language;
        }

        @Override
        public int getEmbeddedStart() {
            return this.start;
        }

        @Override
        public int getCodeEnd() {
            return this.end;
        }

        @Override
        public int getAdditionalCodeLines() {
            return this.additionalCodeLines;
        }

        @Override
        public CharData getCode() {
            return this.code;
        }

    }

    static class HtmlEmbedded extends AbstractEmbedded {
        HtmlEmbedded(final Language language, final int start, final int end, final CharData code) {
            super(language, start, end, 1, code);
        }
    }

    static class LiquidSection extends AbstractEmbedded {
        LiquidSection(final Language language, final int start, final int end, final CharData code) {
            super(language, start, end, 1, code);
        }
    }

    static class MarkdownCodeBlock extends AbstractEmbedded {
        MarkdownCodeBlock(final Language language, final int start, final int end, final int codeLines,
                          final CharData code) {
            super(language, start, end, codeLines, code);
        }
    }

    static class RustLineDoc extends AbstractEmbedded {
        RustLineDoc(final Language language, final int start, final int end, final List<CharData> lines) {
            super(language, start, end, 0, new CharData(lines));
        }
    }


    private Embedding() {
    }

    /**
     * Attempts to find embedded content in the specified region of character data.
     *
     * @param language Language of the file in which content may be embedded
     * @param lines Character data
     * @param start Starting position in the character data in which to look for embedded content
     * @param end Ending position in the character data in which to look for embedded content
     * @return Information about the embedded content, if found. The information returned is
     *      relative to the start of the specified data.
     */
    @Nullable
    static Embedded find(final Language language, final CharData lines, final int start, final int end) {
        return (language.getEmbedSyntax() == null)
               ? null
               : switch (language.getEmbedSyntax()) {
            case html -> findHtml(lines, start, end);
            case liquid -> findLiquid(lines, start, end);
            case markdown -> findMarkdown(lines, start, end);
            case rust -> findRust(lines, start);
        };
    }

    /**
     * Attempts to find embedded content in the specified region of HTML character data.
     *
     * @param lines HTML character data
     * @param start Starting position in the character data in which to look for embedded content
     * @param end Ending position in the character data in which to look for embedded content
     * @return Information about the embedded content, if found. The information returned is
     *      relative to the start of the specified data.
     */
    @Nullable
    private static Embedded findHtml(final CharData lines, final int start, final int end) {
        final Embedded embeddedScript = findHtmlScript(lines, start, end);
        if (embeddedScript != null) {
            return embeddedScript;
        }

        final Embedded embeddedStyle = findHtmlStyle(lines, start, end);
        if (embeddedStyle != null) {
            return embeddedStyle;
        }

        final Embedded embeddedSvg = findHtmlSvg(lines, start, end);
        if (embeddedSvg != null) {
            return embeddedSvg;
        }

        return findHtmlTemplate(lines, start, end);
    }

    /**
     * Attempts to find an embedded script in the specified region of HTML character data.
     *
     * @param lines HTML character data
     * @param start Starting position in the character data in which to look for an embedded script
     * @param end Ending position in the character data in which to look for an embedded script
     * @return Information about the embedded script, if found. The information returned is
     *      relative to the start of the specified data.
     */
    @Nullable
    private static Embedded findHtmlScript(final CharData lines, final int start, final int end) {
        final CharData window1 = lines.subSequence(start, end);
        final Matcher scriptStartMatcher = window1.matcher(HTML_SCRIPT_START_REGEX);
        if (scriptStartMatcher.find()) {
            final int scriptStart = start + scriptStartMatcher.start();
            final int codeStart = start + scriptStartMatcher.end();

            String mimeType = scriptStartMatcher.group(1);
            Optional<Language> languageOpt = Optional.empty();
            if (mimeType != null) {
                mimeType = mimeType.trim();
                if (!mimeType.isEmpty()) {
                    languageOpt = Language.fromMime(mimeType);
                }
            }
            final Language language = languageOpt.orElse(Language.JavaScript);

            final CharData window2 = lines.subSequence(codeStart);
            final Matcher scriptEndMatcher = window2.matcher(HTML_SCRIPT_END_REGEX);
            if (scriptEndMatcher.find()) {
                final int codeEnd = codeStart + scriptEndMatcher.start();
                final CharData code = lines.subSequence(codeStart, codeEnd).trimFirstLastLine();
                if (!code.isBlank()) {
                    return new HtmlEmbedded(language, scriptStart, codeEnd, code);
                }
            }
        }
        return null;
    }

    /**
     * Attempts to find an embedded style sheet in the specified region of HTML character data.
     *
     * @param lines HTML character data
     * @param start Starting position in the character data in which to look for an embedded style sheet
     * @param end Ending position in the character data in which to look for an embedded style sheet
     * @return Information about the embedded style sheet, if found. The information returned is
     *      relative to the start of the specified data.
     */
    @Nullable
    private static Embedded findHtmlStyle(final CharData lines, final int start, final int end) {
        final CharData window1 = lines.subSequence(start, end);
        final Matcher styleStartMatcher = window1.matcher(HTML_STYLE_START_REGEX);
        if (styleStartMatcher.find()) {
            final int styleStart = start + styleStartMatcher.start();
            final int codeStart = start + styleStartMatcher.end();

            String styleType = styleStartMatcher.group(1);
            Optional<Language> languageOpt = Optional.empty();
            if (styleType != null) {
                styleType = styleType.trim();
                if (!styleType.isEmpty()) {
                    languageOpt = Language.fromMime(styleType);
                    if (languageOpt.isEmpty()) {
                        languageOpt = Language.fromDisplayName(styleType);
                    }
                }
            }
            final Language language = languageOpt.orElse(Language.Css);

            final CharData window2 = lines.subSequence(codeStart);
            final Matcher styleEndMatcher = window2.matcher(HTML_STYLE_END_REGEX);
            if (styleEndMatcher.find()) {
                final int codeEnd = codeStart + styleEndMatcher.start();
                final CharData code = lines.subSequence(codeStart, codeEnd).trimFirstLastLine();
                if (!code.isBlank()) {
                    return new HtmlEmbedded(language, styleStart, codeEnd, code);
                }
            }
        }
        return null;
    }

    /**
     * Attempts to find embedded svg in the specified region of HTML character data.
     *
     * @param lines HTML character data
     * @param start Starting position in the character data in which to look for embedded svg
     * @param end Ending position in the character data in which to look for embedded svg
     * @return Information about the embedded svg, if found. The information returned is
     *      relative to the start of the specified data.
     */
    @Nullable
    private static Embedded findHtmlSvg(final CharData lines, final int start, final int end) {
        final CharData window1 = lines.subSequence(start, end);
        final Matcher svgStartMatcher = window1.matcher(HTML_SVG_START_REGEX);
        if (svgStartMatcher.find()) {
            final int svgStart = start + svgStartMatcher.start();
            final int codeStart = start + svgStartMatcher.end();

            final CharData window2 = lines.subSequence(codeStart);
            final Matcher svgEndMatcher = window2.matcher(HTML_SVG_END_REGEX);
            if (svgEndMatcher.find()) {
                final int codeEnd = codeStart + svgEndMatcher.start();
                final CharData code = lines.subSequence(codeStart, codeEnd).trimFirstLastLine();
                if (!code.isBlank()) {
                    return new HtmlEmbedded(Language.Svg, svgStart, codeEnd, code);
                }
            }
        }
        return null;
    }

    /**
     * Attempts to find an embedded template in the specified region of HTML character data.
     *
     * @param lines HTML character data
     * @param start Starting position in the character data in which to look for an embedded template
     * @param end Ending position in the character data in which to look for an embedded template
     * @return Information about the embedded template, if found. The information returned is
     *      relative to the start of the specified data.
     */
    @Nullable
    private static Embedded findHtmlTemplate(final CharData lines, final int start, final int end) {
        final CharData window1 = lines.subSequence(start, end);
        final Matcher templateStartMatcher = window1.matcher(HTML_TEMPLATE_START_REGEX);
        if (templateStartMatcher.find()) {
            final int templateStart = start + templateStartMatcher.start();
            final int codeStart = start + templateStartMatcher.end();

            String templateType = templateStartMatcher.group(1);
            Optional<Language> languageOpt = Optional.empty();
            if (templateType != null) {
                templateType = templateType.trim();
                if (!templateType.isEmpty()) {
                    languageOpt = Language.fromDisplayName(templateType);
                }
            }
            final Language language = languageOpt.orElse(Language.Html);

            final CharData window2 = lines.subSequence(codeStart);
            final Matcher templateEndMatcher = window2.matcher(HTML_TEMPLATE_END_REGEX);
            if (templateEndMatcher.find()) {
                final int codeEnd = codeStart + templateEndMatcher.start();
                final CharData code = lines.subSequence(codeStart, codeEnd).trimFirstLastLine();
                if (!code.isBlank()) {
                    return new HtmlEmbedded(language, templateStart, codeEnd, code);
                }
            }
        }
        return null;
    }

    /**
     * Attempts to find sections in the specified region of Liquid character data.
     *
     * @param lines Liquid character data
     * @param start Starting position in the character data in which to look for section content
     * @param end Ending position in the character data in which to look for section content
     * @return Information about the section content, if found. The information returned is
     *      relative to the start of the specified data.
     */
    @Nullable
    private static Embedded findLiquid(final CharData lines, final int start, final int end) {
        final Embedded embeddedSchema = findLiquidSection(lines, start, end, LIQUID_SCHEMA_START_REGEX,
                                                          LIQUID_SCHEMA_END_REGEX, Language.Json);
        if (embeddedSchema != null) {
            return embeddedSchema;
        }

        final Embedded embeddedJavascript = findLiquidSection(lines, start, end, LIQUID_JAVASCRIPT_START_REGEX,
                                                              LIQUID_JAVASCRIPT_END_REGEX, Language.JavaScript);
        if (embeddedJavascript != null) {
            return embeddedJavascript;
        }

        return findLiquidSection(lines, start, end, LIQUID_STYLESHEET_START_REGEX, LIQUID_STYLESHEET_END_REGEX,
                                 Language.Css);
    }

    /**
     * Attempts to find a section in the specified region of Liquid character data.
     *
     * @param lines Liquid character data
     * @param start Starting position in the character data in which to look for a section
     * @param end Ending position in the character data in which to look for a section
     * @return Information about the section, if found. The information returned is relative to
     *      the start of the specified data.
     */
    @Nullable
    private static Embedded findLiquidSection(final CharData lines, final int start, final int end,
                                              final Pattern startRegex, final Pattern endRegex,
                                              final Language embeddedLanguage) {
        final CharData window1 = lines.subSequence(start, end);
        final Matcher sectionStartMatcher = window1.matcher(startRegex);
        if (sectionStartMatcher.find()) {
            final int schemaStart = start + sectionStartMatcher.start();
            final int codeStart = start + sectionStartMatcher.end();

            final CharData window2 = lines.subSequence(codeStart);
            final Matcher sectionEndMatcher = window2.matcher(endRegex);
            if (sectionEndMatcher.find()) {
                final int codeEnd = codeStart + sectionEndMatcher.start();
                final CharData code = lines.subSequence(codeStart, codeEnd).trimFirstLastLine();
                if (!code.isBlank()) {
                    return new LiquidSection(embeddedLanguage, schemaStart, codeEnd, code);
                }
            }
        }
        return null;
    }

    /**
     * Attempts to find embedded content in the specified region of Markdown character data.
     *
     * @param lines Markdown character data
     * @param start Starting position in the character data in which to look for embedded content
     * @param end Ending position in the character data in which to look for embedded content
     * @return Information about the embedded content, if found. The information returned is
     *      relative to the start of the specified data.
     */
    @Nullable
    private static Embedded findMarkdown(final CharData lines, final int start, final int end) {
        final CharData window1 = lines.subSequence(start, end);
        final Matcher blockStartMatcher = window1.matcher(MARKDOWN_CODE_START_REGEX);
        if (blockStartMatcher.find()) {
            Optional<Language> languageOpt = Optional.empty();
            final String[] blockLanguages = COMMA_REGEX.split(blockStartMatcher.group(2).trim());
            for (final String blockLanguage : blockLanguages) {
                languageOpt = Language.fromDisplayName(blockLanguage).or(() -> Language.fromId(blockLanguage));
                if (languageOpt.isPresent()) {
                    break;
                }
            }
            if (languageOpt.isEmpty()) {
                return null;
            }
            final Language language = languageOpt.get();

            final int blockStart = start + blockStartMatcher.start();
            final int codeStart = start + blockStartMatcher.end();
            final String delimiter = blockStartMatcher.group(1);

            final CharData window2 = lines.subSequence(codeStart);
            final Matcher blockEndMatcher = window2.matcher(MARKDOWN_BLOCK_DELIM_1.equals(delimiter)
                                                            ? MARKDOWN_CODE_END_1_REGEX
                                                            : MARKDOWN_CODE_END_2_REGEX);
            if (blockEndMatcher.find()) {
                final int codeEnd = codeStart + blockEndMatcher.start();
                final int blockEnd = codeStart + blockEndMatcher.end();
                final CharData code = lines.subSequence(codeStart, codeEnd).trimLastLine();
                if (!code.isBlank()) {
                    return new MarkdownCodeBlock(language, blockStart, blockEnd, 2, code);
                }
            } else {
                final int codeEnd = lines.length();
                final CharData code = lines.subSequence(codeStart, codeEnd).trimLastLine();
                if (!code.isBlank()) {
                    return new MarkdownCodeBlock(language, blockStart, codeEnd, 1, code);
                }
            }
        }
        return null;
    }

    /**
     * Attempts to find embedded content in the specified region of Rust character data.
     *
     * @param lines Rust character data
     * @param start Starting position in the character data in which to look for embedded content
     * @return Information about the embedded content, if found. The information returned is
     *      relative to the start of the specified data.
     */
    @Nullable
    private static Embedded findRust(final CharData lines, final int start) {
        final CharData window = lines.subSequence(start);

        final CharData trimmedWindow = window.trimLeading();
        String commentDelim = null;
        if (trimmedWindow.startsWith(RUST_OUTER_LINE_DOC)) {
            commentDelim = RUST_OUTER_LINE_DOC;
        } else if (trimmedWindow.startsWith(RUST_INNER_LINE_DOC)) {
            commentDelim = RUST_INNER_LINE_DOC;
        }
        if (commentDelim == null) {
            return null;
        }

        final List<CharData> mdLines = new ArrayList<>();
        final CharData.LineIterator iterator = window.lineIterator();
        int blockEnd = lines.length();

        while (iterator.hasNext()) {
            final CharData line = iterator.next().trimLeading();
            if (line.startsWith(commentDelim)) {
                final int lineStart = Math.min(commentDelim.length(), line.length());
                final CharData strippedLine = line.subSequence(lineStart);
                mdLines.add(strippedLine);
                blockEnd = start + iterator.getEnd();
            } else {
                blockEnd = start + iterator.getStart();
                break;
            }
        }

        if (!mdLines.isEmpty()) {
            final int lastIndex = mdLines.size() - 1;
            mdLines.set(lastIndex, mdLines.get(lastIndex).trimTrailing());
            return new RustLineDoc(Language.Markdown, start, blockEnd, mdLines);
        }

        return null;
    }
}
