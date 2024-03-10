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
    Map<Language, Stats> countsD0F1;
    Map<Language, Stats> countsD0F2;
    Map<Language, Stats> countsD1F1;
    Map<Language, Stats> countsD1F2;
    Map<Language, Stats> countsD1F3;
    Map<Language, Stats> countsD1F4;
    Map<Language, Stats> countsD2F1;
    Map<Language, Stats> countsD2F2;
    Map<Language, Stats> countsD2F3;
    Map<Language, Stats> countsD3F1;
    Map<Language, Stats> countsD3F2A;
    Map<Language, Stats> countsD3F2B;

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

        this.countsD0F1 = Map.of(Java, new Stats(26, 9, 9));
        this.countsD0F2 = Map.of(Html, new Stats(19, 7, 4), Css, new Stats(3, 5, 0), JavaScript, new Stats(1, 6, 0));
        this.countsD1F1 = Map.of(Java, new Stats(26, 9, 9));
        this.countsD1F2 = Map.of(Cpp, new Stats(37, 2, 6));
        this.countsD1F3 = Map.of(C, new Stats(33, 7, 8));
        this.countsD1F4 = Map.of();
        this.countsD2F1 = Map.of(JavaScript, new Stats(14, 11, 6));
        this.countsD2F2 = Map.of(Ruby, new Stats(9, 7, 3));
        this.countsD2F3 = Map.of(Cpp, new Stats(37, 2, 6));
        this.countsD3F1 = Map.of(TypeScript, new Stats(20, 9, 3));
        this.countsD3F2A = Map.of(Python, new Stats(5, 6, 1));
        this.countsD3F2B = Map.of(Python, new Stats(10, 1, 1));
    }
}
