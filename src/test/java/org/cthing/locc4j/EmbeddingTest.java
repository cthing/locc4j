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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class EmbeddingTest {

    @Nested
    class FindHtmlTest {
        @Nested
        class FindScriptTest {
            @Test
            public void testFindDefault() {
                final String content = """
                        <p>This is a test</p>
                        <script>
                        var i = 21;
                        </script>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 4,
                                                                   content.length() - 2);
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.JavaScript);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(43);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("var i = 21;");
            }

            @Test
            public void testFindType() {
                final String content = """
                        <p>This is a test</p>
                        <script type="text/plain">
                        Hello world
                        </script>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Text);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(61);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("Hello world");
            }

            @Test
            public void testFindBadType() {
                final String content = """
                        <p>This is a test</p>
                        <script type="notfound/foobar">
                        Hello world
                        </script>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.JavaScript);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(66);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("Hello world");
            }

            @Test
            public void testFindMissingEnd() {
                final String content = """
                        <p>This is a test</p>
                        <script">
                        Hello world
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }

            @Test
            public void testFindBlank() {
                final String content = """
                        <p>This is a test</p>
                        <script>      </script>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }
        }

        @Nested
        class FindStyleTest {
            @Test
            public void testFindDefault() {
                final String content = """
                    <p>This is a test</p>
                    <style>
                    color: red;
                    </style>
                    <h1>Heading</h1>
                    """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 4,
                                                                   content.length() - 2);
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Css);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(42);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("color: red;");
            }

            @Test
            public void testFindType() {
                final String content = """
                        <p>This is a test</p>
                        <style type="text/plain">
                        Hello world
                        </style>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Text);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(60);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("Hello world");
            }

            @Test
            public void testFindLang() {
                final String content = """
                        <p>This is a test</p>
                        <style lang="yaml">
                        Hello world
                        </style>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Yaml);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(54);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("Hello world");
            }

            @Test
            public void testFindBadType() {
                final String content = """
                        <p>This is a test</p>
                        <style type="notfound/foobar">
                        Hello world
                        </style>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Css);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(65);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("Hello world");
            }

            @Test
            public void testFindMissingEnd() {
                final String content = """
                        <p>This is a test</p>
                        <style>
                        Hello world
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }

            @Test
            public void testFindBlank() {
                final String content = """
                        <p>This is a test</p>
                        <style>      </style>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }
        }

        @Nested
        class FindSvgTest {
            @Test
            public void testFindDefault() {
                final String content = """
                    <p>This is a test</p>
                    <svg width="100" height="100">
                        <circle cx="50" cy="50" r="40" stroke="green" stroke-width="4" fill="yellow" />
                        <polygon points="200,10 250,190 150,190" style="fill:lime;stroke:purple;stroke-width:3" />
                    </svg>
                    <h1>Heading</h1>
                    """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Svg);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(232);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("    <circle cx=\"50\" cy=\"50\" r=\"40\" stroke=\"green\" stroke-width=\"4\" fill=\"yellow\" />\n"
                                                                    + "    <polygon points=\"200,10 250,190 150,190\" style=\"fill:lime;stroke:purple;stroke-width:3\" />");
            }

            @Test
            public void testFindMissingEnd() {
                final String content = """
                        <p>This is a test</p>
                        <svg>
                        Hello world
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }

            @Test
            public void testFindBlank() {
                final String content = """
                        <p>This is a test</p>
                        <svg>      </svg>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }
        }

        @Nested
        class FindTemplateTest {
            @Test
            public void testFindDefault() {
                final String content = """
                    <p>This is a test</p>
                    <template id="footer">
                    <p>Footer</p>
                    </template>
                    <h1>Heading</h1>
                    """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 4,
                                                                   content.length() - 2);
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Html);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(59);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("<p>Footer</p>");
            }

            @Test
            public void testFindLang() {
                final String content = """
                        <p>This is a test</p>
                        <template id="footer" lang="yaml">
                        Hello world
                        </template>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Yaml);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(69);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("Hello world");
            }

            @Test
            public void testFindBadLang() {
                final String content = """
                        <p>This is a test</p>
                        <template lang="notfound">
                        Hello world
                        </template>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Html);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(61);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("Hello world");
            }

            @Test
            public void testFindMissingEnd() {
                final String content = """
                        <p>This is a test</p>
                        <template>
                        Hello world
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }

            @Test
            public void testFindBlank() {
                final String content = """
                        <p>This is a test</p>
                        <template>      </template>
                        <h1>Heading</h1>
                        """;
                final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }
        }

        @Test
        public void testFindNothing() {
            final String content = "<p>This is a test</p>";
            final Embedding.Embedded embedded = Embedding.find(Language.Html, data(content), 0, content.length());
            assertThat(embedded).isNull();
        }
    }

    @Nested
    class FindLiquidTest {

        @Nested
        class FindSchemaTest {
            @Test
            public void testFindDefault() {
                final String content = """
                                       <p>This is a test</p>
                                       {% schema %}
                                       {
                                           "name": "foo",
                                           "tag": "section"
                                       }
                                       {% endschema %}
                                       <h1>Heading</h1>
                                       """;
                final Embedding.Embedded embedded = Embedding.find(Language.Liquid, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Json);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(79);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("""
                                                                    {
                                                                        "name": "foo",
                                                                        "tag": "section"
                                                                    }""");
            }

            @Test
            public void testFindMissingEnd() {
                final String content = """
                                       <p>This is a test</p>
                                       {% schema %}
                                       {
                                           "name": "foo",
                                           "tag": "section"
                                       }
                                       <h1>Heading</h1>
                                       """;
                final Embedding.Embedded embedded = Embedding.find(Language.Liquid, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }

            @Test
            public void testFindBlank() {
                final String content = """
                                       <p>This is a test</p>
                                       {% schema %}
                                       {% endschema %}
                                       <h1>Heading</h1>
                                       """;
                final Embedding.Embedded embedded = Embedding.find(Language.Liquid, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }
        }

        @Nested
        class FindJavascriptTest {
            @Test
            public void testFindDefault() {
                final String content = """
                                       <p>This is a test</p>
                                       {% javascript %}
                                       var i = 10;
                                       {% endjavascript %}
                                       <h1>Heading</h1>
                                       """;
                final Embedding.Embedded embedded = Embedding.find(Language.Liquid, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.JavaScript);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(51);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("var i = 10;");
            }

            @Test
            public void testFindMissingEnd() {
                final String content = """
                                       <p>This is a test</p>
                                       {% javascript %}
                                       var i = 10;
                                       <h1>Heading</h1>
                                       """;
                final Embedding.Embedded embedded = Embedding.find(Language.Liquid, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }

            @Test
            public void testFindBlank() {
                final String content = """
                                       <p>This is a test</p>
                                       {% javascript %}
                                       {% endjavascript %}
                                       <h1>Heading</h1>
                                       """;
                final Embedding.Embedded embedded = Embedding.find(Language.Liquid, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }
        }

        @Nested
        class FindStylesheetTest {
            @Test
            public void testFindDefault() {
                final String content = """
                                       <p>This is a test</p>
                                       {% stylesheet %}
                                       color: red;
                                       {% endstylesheet %}
                                       <h1>Heading</h1>
                                       """;
                final Embedding.Embedded embedded = Embedding.find(Language.Liquid, data(content), 0, content.length());
                assertThat(embedded).isNotNull();
                assertThat(embedded.getLanguage()).isEqualTo(Language.Css);
                assertThat(embedded.getEmbeddedStart()).isEqualTo(22);
                assertThat(embedded.getCodeEnd()).isEqualTo(51);
                assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
                assertThat(embedded.getCode().toString()).isEqualTo("color: red;");
            }

            @Test
            public void testFindMissingEnd() {
                final String content = """
                                       <p>This is a test</p>
                                       {% stylesheet %}
                                       color: red;
                                       <h1>Heading</h1>
                                       """;
                final Embedding.Embedded embedded = Embedding.find(Language.Liquid, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }

            @Test
            public void testFindBlank() {
                final String content = """
                                       <p>This is a test</p>
                                       {% stylesheet %}
                                       {% endstylesheet %}
                                       <h1>Heading</h1>
                                       """;
                final Embedding.Embedded embedded = Embedding.find(Language.Liquid, data(content), 0, content.length());
                assertThat(embedded).isNull();
            }
        }
    }

    @Nested
    class FindMarkdownTest {
        @Test
        public void testNoLanguage() {
            final String content = """
                        This is a test
                        ```
                        int i = 12;
                        ```
                        # Heading
                        """;
            final Embedding.Embedded embedded = Embedding.find(Language.Markdown, data(content), 0, content.length());
            assertThat(embedded).isNull();
        }

        @Test
        public void testUnknownLanguage() {
            final String content = """
                        This is a test
                        ```foobar
                        int i = 12;
                        ```
                        # Heading
                        """;
            final Embedding.Embedded embedded = Embedding.find(Language.Markdown, data(content), 0, content.length());
            assertThat(embedded).isNull();
        }

        @Test
        public void testSingleLanguage() {
            final String content = """
                        This is a test
                        ```java
                        int i = 12;
                        ```
                        # Heading
                        """;
            final Embedding.Embedded embedded = Embedding.find(Language.Markdown, data(content), 0, content.length());
            assertThat(embedded).isNotNull();
            assertThat(embedded.getLanguage()).isEqualTo(Language.Java);
            assertThat(embedded.getEmbeddedStart()).isEqualTo(15);
            assertThat(embedded.getCodeEnd()).isEqualTo(39);
            assertThat(embedded.getAdditionalCodeLines()).isEqualTo(2);
            assertThat(embedded.getCode().toString()).isEqualTo("int i = 12;");
        }

        @Test
        public void testMultipleLanguages() {
            final String content = """
                        This is a test
                        ```foobar,java
                        int i = 12;
                        ```
                        # Heading
                        """;
            final Embedding.Embedded embedded = Embedding.find(Language.Markdown, data(content), 0, content.length());
            assertThat(embedded).isNotNull();
            assertThat(embedded.getLanguage()).isEqualTo(Language.Java);
            assertThat(embedded.getEmbeddedStart()).isEqualTo(15);
            assertThat(embedded.getCodeEnd()).isEqualTo(46);
            assertThat(embedded.getAdditionalCodeLines()).isEqualTo(2);
            assertThat(embedded.getCode().toString()).isEqualTo("int i = 12;");
        }

        @Test
        public void testSingleLanguageAltSyntax() {
            final String content = """
                        This is a test
                        ~~~java
                        int i = 12;
                        ~~~
                        # Heading
                        """;
            final Embedding.Embedded embedded = Embedding.find(Language.Markdown, data(content), 0, content.length());
            assertThat(embedded).isNotNull();
            assertThat(embedded.getLanguage()).isEqualTo(Language.Java);
            assertThat(embedded.getEmbeddedStart()).isEqualTo(15);
            assertThat(embedded.getCodeEnd()).isEqualTo(39);
            assertThat(embedded.getAdditionalCodeLines()).isEqualTo(2);
            assertThat(embedded.getCode().toString()).isEqualTo("int i = 12;");
        }

        @Test
        public void testMissingEnd() {
            final String content = """
                        This is a test
                        ```java
                        int i = 12;
                        """;
            final Embedding.Embedded embedded = Embedding.find(Language.Markdown, data(content), 0, content.length());
            assertThat(embedded).isNotNull();
            assertThat(embedded.getLanguage()).isEqualTo(Language.Java);
            assertThat(embedded.getEmbeddedStart()).isEqualTo(15);
            assertThat(embedded.getCodeEnd()).isEqualTo(35);
            assertThat(embedded.getAdditionalCodeLines()).isEqualTo(1);
            assertThat(embedded.getCode().toString()).isEqualTo("int i = 12;");
        }

        @Test
        public void testFindNothing() {
            final String content = "This is a test";
            final Embedding.Embedded embedded = Embedding.find(Language.Markdown, data(content), 0, content.length());
            assertThat(embedded).isNull();
        }
    }

    @Nested
    class FindRustTest {
        @Test
        public void testOuterDoc() {
            final String content = """
                                   /// # Hello World
                                   /// This is a test
                                   """;
            final Embedding.Embedded embedded = Embedding.find(Language.Rust, data(content), 0, content.length());
            assertThat(embedded).isNotNull();
            assertThat(embedded.getLanguage()).isEqualTo(Language.Markdown);
            assertThat(embedded.getEmbeddedStart()).isEqualTo(0);
            assertThat(embedded.getCodeEnd()).isEqualTo(37);
            assertThat(embedded.getAdditionalCodeLines()).isEqualTo(0);
            assertThat(embedded.getCode().toString()).isEqualTo(" # Hello World\n This is a test");
        }

        @Test
        public void testInnerDoc() {
            final String content = """
                                   //! # Hello World
                                   //! This is a test
                                   """;
            final Embedding.Embedded embedded = Embedding.find(Language.Rust, data(content), 0, content.length());
            assertThat(embedded).isNotNull();
            assertThat(embedded.getLanguage()).isEqualTo(Language.Markdown);
            assertThat(embedded.getEmbeddedStart()).isEqualTo(0);
            assertThat(embedded.getCodeEnd()).isEqualTo(37);
            assertThat(embedded.getAdditionalCodeLines()).isEqualTo(0);
            assertThat(embedded.getCode().toString()).isEqualTo(" # Hello World\n This is a test");
        }

        @Test
        public void testWithBlanks() {
            final String content = """
                                   //! # Hello World
                                   //!
                                   //! This is a test
                                   """;
            final Embedding.Embedded embedded = Embedding.find(Language.Rust, data(content), 0, content.length());
            assertThat(embedded).isNotNull();
            assertThat(embedded.getLanguage()).isEqualTo(Language.Markdown);
            assertThat(embedded.getEmbeddedStart()).isEqualTo(0);
            assertThat(embedded.getCodeEnd()).isEqualTo(41);
            assertThat(embedded.getAdditionalCodeLines()).isEqualTo(0);
            assertThat(embedded.getCode().toString()).isEqualTo(" # Hello World\n\n This is a test");
        }

        @Test
        public void testFindNothing() {
            final String content = "This is a test";
            final Embedding.Embedded embedded = Embedding.find(Language.Rust, data(content), 0, content.length());
            assertThat(embedded).isNull();
        }
    }

    private CharData data(final String str) {
        return new CharData(str.toCharArray());
    }
}
