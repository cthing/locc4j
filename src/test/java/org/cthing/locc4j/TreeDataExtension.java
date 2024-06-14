/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.locc4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.cthing.locc4j.Language.*;


@SuppressWarnings("NotNullFieldNotInitialized")
public class TreeDataExtension implements BeforeAllCallback {

    Path start;
    Path fileD0F1;
    Path fileD0F2;
    Path fileD1F1;
    Path fileD1F2;
    Path fileD1F3;
    Path fileD1F4;
    Path fileD2F1;
    Path fileD2F2;
    Path fileD2F3;
    Path fileD3F1;
    Path fileD3F2;
    Map<Language, Counts> countsD0F1;
    Map<Language, Counts> countsD0F2;
    Map<Language, Counts> countsD1F1;
    Map<Language, Counts> countsD1F2;
    Map<Language, Counts> countsD1F3;
    Map<Language, Counts> countsD1F4;
    Map<Language, Counts> countsD2F1;
    Map<Language, Counts> countsD2F2;
    Map<Language, Counts> countsD2F3;
    Map<Language, Counts> countsD3F1;
    Map<Language, Counts> countsD3F2A;
    Map<Language, Counts> countsD3F2B;

    @Override
    public void beforeAll(final ExtensionContext context) {
        final URL url = CountingTreeWalkerTest.class.getResource("/tree");
        assert url != null;
        this.start  = Path.of(url.getPath());

        this.fileD0F1 = this.start.resolve("file-0-1.java");
        this.fileD0F2 = this.start.resolve("file-0-2.html");
        this.fileD1F1 = this.start.resolve("dir1/file-1-1.java");
        this.fileD1F2 = this.start.resolve("dir1/file-1-2.cpp");
        this.fileD1F3 = this.start.resolve("dir1/file-1-3.c");
        this.fileD1F4 = this.start.resolve("dir1/file-1-4.unknown");
        this.fileD2F1 = this.start.resolve(".dir2/file-2-1.js");
        this.fileD2F2 = this.start.resolve(".dir2/file-2-2.rb");
        this.fileD2F3 = this.start.resolve(".dir2/file-2-3.cpp");
        this.fileD3F1 = this.start.resolve("dir1/dir3/file-3-1.ts");
        this.fileD3F2 = this.start.resolve("dir1/dir3/file-3-2.py");

        this.countsD0F1 = Map.of(Java, new Counts(26, 9, 9));
        this.countsD0F2 = Map.of(Html, new Counts(19, 7, 4), Css, new Counts(3, 5, 0), JavaScript, new Counts(1, 6, 0));
        this.countsD1F1 = Map.of(Java, new Counts(26, 9, 9));
        this.countsD1F2 = Map.of(Cpp, new Counts(37, 2, 6));
        this.countsD1F3 = Map.of(C, new Counts(33, 7, 8));
        this.countsD1F4 = Map.of();
        this.countsD2F1 = Map.of(JavaScript, new Counts(14, 11, 6));
        this.countsD2F2 = Map.of(Ruby, new Counts(9, 7, 3));
        this.countsD2F3 = Map.of(Cpp, new Counts(37, 2, 6));
        this.countsD3F1 = Map.of(TypeScript, new Counts(20, 9, 3));
        this.countsD3F2A = Map.of(Python, new Counts(5, 6, 1));
        this.countsD3F2B = Map.of(Python, new Counts(10, 1, 1));
    }
}
