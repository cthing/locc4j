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
         * Obtains the number of comment lines involved in the embedding. For Markdown, this is the comment block
         * delimiter lines.
         *
         * @return Number of comment lines involved in the embedding.
         */
        int getCommentLines();

        /**
         * Obtains the number of additional code lines involved in the embedding. For HTML, this takes into account
         * the opening embedding tag.
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
        private final int commentLines;
        private final int additionalCodeLines;
        private final CharData code;

        protected AbstractEmbedded(final Language language, final int start, final int end, final int commentLines,
                                   final int additionalCodeLines, final CharData code) {
            this.language = language;
            this.start = start;
            this.end = end;
            this.commentLines = commentLines;
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
        public int getCommentLines() {
            return this.commentLines;
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
            super(language, start, end, 0, 1, code);
        }
    }

    static class MarkdownCodeBlock extends AbstractEmbedded {
        MarkdownCodeBlock(final Language language, final int start, final int end, final int commentLines,
                          final CharData code) {
            super(language, start, end, commentLines, 0, code);
        }
    }

    static class RustLineDoc extends AbstractEmbedded {
        RustLineDoc(final Language language, final int start, final int end, final List<CharData> lines) {
            super(language, start, end, 0, 0, new CharData(lines));
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
     * @return Information about the embedded content, if found.
     */
    static Optional<Embedded> find(final Language language, final CharData lines, final int start, final int end) {
        return language.getEmbedSyntax().flatMap(syntax -> switch (syntax) {
            case html -> findHtml(lines, start, end);
            case markdown -> findMarkdown(lines, start, end);
            case rust -> findRust(lines, start);
        });

    }

    private static Optional<Embedded> findHtml(final CharData lines, final int start, final int end) {
        final Optional<Embedded> embeddedScript = findHtmlScript(lines, start, end);
        if (embeddedScript.isPresent()) {
            return embeddedScript;
        }

        final Optional<Embedded> embeddedStyle = findHtmlStyle(lines, start, end);
        if (embeddedStyle.isPresent()) {
            return embeddedStyle;
        }

        return findHtmlTemplate(lines, start, end);

    }

    private static Optional<Embedded> findHtmlScript(final CharData lines, final int start, final int end) {
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
                final CharData code = lines.subSequence(codeStart, codeEnd);
                if (!code.isBlank()) {
                    return Optional.of(new HtmlEmbedded(language, scriptStart, codeEnd, code));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Embedded> findHtmlStyle(final CharData lines, final int start, final int end) {
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
                    languageOpt = Language.fromName(styleType);
                }
            }
            final Language language = languageOpt.orElse(Language.Css);

            final CharData window2 = lines.subSequence(codeStart);
            final Matcher styleEndMatcher = window2.matcher(HTML_STYLE_END_REGEX);
            if (styleEndMatcher.find()) {
                final int codeEnd = codeStart + styleEndMatcher.start();
                final CharData code = lines.subSequence(codeStart, codeEnd);
                if (!code.isBlank()) {
                    return Optional.of(new HtmlEmbedded(language, styleStart, codeEnd, code));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Embedded> findHtmlTemplate(final CharData lines, final int start, final int end) {
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
                    languageOpt = Language.fromName(templateType);
                }
            }
            final Language language = languageOpt.orElse(Language.Html);

            final CharData window2 = lines.subSequence(codeStart);
            final Matcher templateEndMatcher = window2.matcher(HTML_TEMPLATE_END_REGEX);
            if (templateEndMatcher.find()) {
                final int codeEnd = codeStart + templateEndMatcher.start();
                final CharData code = lines.subSequence(codeStart, codeEnd);
                if (!code.isBlank()) {
                    return Optional.of(new HtmlEmbedded(language, templateStart, codeEnd, code));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Embedded> findMarkdown(final CharData lines, final int start, final int end) {
        final CharData window1 = lines.subSequence(start, end);
        final Matcher blockStartMatcher = window1.matcher(MARKDOWN_CODE_START_REGEX);
        if (blockStartMatcher.find()) {
            Optional<Language> languageOpt = Optional.empty();
            final String[] blockLanguages = COMMA_REGEX.split(blockStartMatcher.group(2).trim());
            for (final String blockLanguage : blockLanguages) {
                languageOpt = Language.fromName(blockLanguage);
                if (languageOpt.isEmpty()) {
                    languageOpt = Language.fromId(blockLanguage);
                }
                if (languageOpt.isPresent()) {
                    break;
                }
            }
            if (languageOpt.isEmpty()) {
                return Optional.empty();
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
                final int codeEnd = start + blockEndMatcher.start();
                final int blockEnd = start + blockEndMatcher.end();
                final CharData code = lines.subSequence(codeStart, codeEnd);
                if (!code.isBlank()) {
                    return Optional.of(new MarkdownCodeBlock(language, blockStart, blockEnd, 2, code));
                }
            } else {
                final int codeEnd = lines.length();
                final CharData code = lines.subSequence(codeStart, codeEnd);
                if (!code.isBlank()) {
                    return Optional.of(new MarkdownCodeBlock(language, blockStart, codeEnd, 1, code));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Embedded> findRust(final CharData lines, final int start) {
        final CharData window = lines.subSequence(start);

        final CharData trimmedWindow = window.trimLeading();
        String commentDelim = null;
        if (trimmedWindow.startsWith(RUST_OUTER_LINE_DOC)) {
            commentDelim = RUST_OUTER_LINE_DOC;
        } else if (trimmedWindow.startsWith(RUST_INNER_LINE_DOC)) {
            commentDelim = RUST_INNER_LINE_DOC;
        }
        if (commentDelim == null) {
            return Optional.empty();
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
            }
        }

        if (!mdLines.isEmpty()) {
            return Optional.of(new RustLineDoc(Language.Markdown, start, blockEnd, mdLines));
        }

        return Optional.empty();
    }
}
