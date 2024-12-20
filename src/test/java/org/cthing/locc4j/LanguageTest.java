/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.locc4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;


public class LanguageTest {

    @TempDir
    Path tempDir;

    @Test
    public void testProperties() {
        final Language css = Language.Css;
        assertThat(css.getDisplayName()).isEqualTo("CSS");
        assertThat(css.getDescription()).isEqualTo("Cascading Style Sheets language");
        assertThat(css.getWebsite()).isEqualTo("https://www.w3.org/Style/CSS/");
        assertThat(css.isLineComment("//"::contentEquals)).isTrue();
        assertThat(css.isNestable()).isFalse();
        assertThat(css.isNestedComment(delim -> "\"".contentEquals(delim.start()))).isFalse();
        assertThat(css.isQuote(delim -> "\"".contentEquals(delim.start()) && "\"".contentEquals(delim.end()))).isTrue();
        assertThat(css.isVerbatimQuote(delim -> "'".contentEquals(delim.start()))).isFalse();
        assertThat(css.isDocQuote(delim -> "'".contentEquals(delim.start()))).isFalse();
        assertThat(css.isColumnSignificant()).isFalse();
        Assertions.<@Nullable Pattern>assertThat(css.getImportantSyntax()).isNotNull();
        assertThat(css.getImportantSyntax().pattern()).isEqualTo("\"|'|/\\*");

        assertThat(Language.Python.isDocQuote(delim -> "\"\"\"".contentEquals(delim.start()))).isTrue();
        assertThat(Language.CSharp.isVerbatimQuote(delim -> "@\"".contentEquals(delim.start()))).isTrue();
        assertThat(Language.D.isAnyMultiLineComment(delim -> "/*".contentEquals(delim.start())
                && "*/".contentEquals(delim.end()))).isTrue();
        assertThat(Language.D.isNestedComment(delim -> "/+".contentEquals(delim.start()))).isTrue();
        assertThat(Language.Elm.isNestable()).isTrue();
        assertThat(Language.FortranLegacy.isColumnSignificant()).isTrue();
    }

    @Test
    public void testImportantSyntax() {
        final Language abnf = Language.ABNF;
        Assertions.<@Nullable Pattern>assertThat(abnf.getImportantSyntax()).isNull();

        final Language asn1 = Language.Asn1;
        Assertions.<@Nullable Pattern>assertThat(asn1.getImportantSyntax()).isNotNull();
        assertThat(asn1.getImportantSyntax().pattern()).isEqualTo("\"|'|/\\*");
        assertThat(asn1.getImportantSyntax().matcher("").find()).isFalse();
        assertThat(asn1.getImportantSyntax().matcher("hello world").find()).isFalse();
        assertThat(asn1.getImportantSyntax().matcher("Hello \"World\"").find()).isTrue();
        assertThat(asn1.getImportantSyntax().matcher("Hello 'World'").find()).isTrue();
        assertThat(asn1.getImportantSyntax().matcher("Hello /* World */").find()).isTrue();
    }

    @Test
    public void testFromShebang() throws IOException {
        final Path shFile = Files.createFile(this.tempDir.resolve("test.sh"));
        Files.writeString(shFile, "#!/bin/sh\necho 'hello'");
        assertThat(Language.fromShebang(shFile)).contains(Language.Sh);

        final Path pythonFile = Files.createFile(this.tempDir.resolve("test.py"));
        Files.writeString(pythonFile, "#!/usr/bin/env python\nprint('Hello')");
        assertThat(Language.fromShebang(pythonFile)).contains(Language.Python);

        final Path unknownInterpreterFile = Files.createFile(this.tempDir.resolve("test.foo"));
        Files.writeString(unknownInterpreterFile, "#!/bin/foo\necho 'hello'");
        assertThat(Language.fromShebang(unknownInterpreterFile)).isEmpty();

        final Path missingInterpreterFile = Files.createFile(this.tempDir.resolve("test.junk"));
        Files.writeString(missingInterpreterFile, "#");
        assertThat(Language.fromShebang(missingInterpreterFile)).isEmpty();

        final Path unknownEnvFile = Files.createFile(this.tempDir.resolve("test.bar"));
        Files.writeString(unknownEnvFile, "#!/usr/bin/env bar\nprint('Hello')");
        assertThat(Language.fromShebang(unknownEnvFile)).isEmpty();

        final Path missingEnvFile = Files.createFile(this.tempDir.resolve("test.xyz"));
        Files.writeString(missingEnvFile, "#!/usr/bin/env\nprint('Hello')");
        assertThat(Language.fromShebang(missingEnvFile)).isEmpty();

        final Path emptyFile = Files.createFile(this.tempDir.resolve("test.lmn"));
        assertThat(Language.fromShebang(emptyFile)).isEmpty();

        final Path missingFile = Path.of("/tmp/locc4j__NOT_FOUND_____");
        assertThat(Language.fromShebang(missingFile)).isEmpty();
    }

    @Test
    public void testFromId() {
        assertThat(Language.fromId("css")).contains(Language.Css);
        assertThat(Language.fromId("__XXYYZZ")).isEmpty();
    }

    @Test
    public void testFromDisplayName() {
        assertThat(Language.fromDisplayName("Visual Studio Solution")).contains(Language.VisualStudioSolution);
        assertThat(Language.fromDisplayName("__XXYYZZ")).isEmpty();
    }

    @Test
    public void testFromFileExtension() {
        assertThat(Language.fromFileExtension("xsl")).contains(Language.XSL);
        assertThat(Language.fromFileExtension("xslt")).contains(Language.XSL);
        assertThat(Language.fromFileExtension("foobar17")).isEmpty();
    }

    @Test
    public void testFromMime() {
        assertThat(Language.fromMime("application/json")).contains(Language.Json);
        assertThat(Language.fromMime("application/x-ecmascript")).contains(Language.JavaScript);
        assertThat(Language.fromMime("zzyzzx")).isEmpty();
    }

    @Test
    public void testFromFile() throws IOException {
        final Path makefile = Files.createFile(this.tempDir.resolve("Makefile"));
        assertThat(Language.fromFile(makefile)).contains(Language.Makefile);

        final Path pythonFile = Files.createFile(this.tempDir.resolve("foo.py"));
        assertThat(Language.fromFile(pythonFile)).contains(Language.Python);

        final Path shFile = Files.createFile(this.tempDir.resolve("test.sh"));
        Files.writeString(shFile, "#!/bin/sh\necho 'hello'");
        assertThat(Language.fromFile(shFile)).contains(Language.Sh);

        final Path missingFile = Path.of("/tmp/locc4j__NOT_FOUND_____");
        assertThat(Language.fromFile(missingFile)).isEmpty();
    }

    @Test
    public void testFileExtensionRemapping() {
        assertThat(Language.getExtensions().get("c")).isEqualTo(Language.C);
        Language.addExtension("c", Language.Cpp);
        assertThat(Language.getExtensions().get("c")).isEqualTo(Language.Cpp);
        Language.addExtension("C", Language.Html);
        assertThat(Language.getExtensions().get("c")).isEqualTo(Language.Html);
        Language.removeExtension("c");
        Assertions.<@Nullable Language>assertThat(Language.getExtensions().get("c")).isNull();
        Language.addExtension("zzz", Language.Python);
        assertThat(Language.getExtensions().get("zzz")).isEqualTo(Language.Python);
        Language.resetExtensions();
        assertThat(Language.getExtensions().get("c")).isEqualTo(Language.C);
        Assertions.<@Nullable Language>assertThat(Language.getExtensions().get("zzz")).isNull();
    }
}
