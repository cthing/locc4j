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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cthing.locc4j.Language.*;


@SuppressWarnings("NotNullFieldNotInitialized")
public class CountingTreeWalkerTest {

    private static Path start;
    private static Path fileD0F1;
    private static Path fileD0F2;
    private static Path fileD1F1;
    private static Path fileD1F2;
    private static Path fileD1F3;
    private static Path fileD2F1;
    private static Path fileD2F2;
    private static Path fileD2F3;
    private static Path fileD3F1;
    private static Path fileD3F2;
    private static Map<Language, Stats> countsD0F1;
    private static Map<Language, Stats> countsD0F2;
    private static Map<Language, Stats> countsD1F1;
    private static Map<Language, Stats> countsD1F2;
    private static Map<Language, Stats> countsD1F3;
    private static Map<Language, Stats> countsD2F1;
    private static Map<Language, Stats> countsD2F2;
    private static Map<Language, Stats> countsD2F3;
    private static Map<Language, Stats> countsD3F1;
    private static Map<Language, Stats> countsD3F2A;
    private static Map<Language, Stats> countsD3F2B;

    @BeforeAll
    public static void setupAll() {
        final URL url = CountingTreeWalkerTest.class.getResource("/tree");
        assert url != null;
        start  = Path.of(url.getPath());

        fileD0F1 = start.resolve("file-0-1.java");
        fileD0F2 = start.resolve("file-0-2.html");
        fileD1F1 = start.resolve("dir1/file-1-1.java");
        fileD1F2 = start.resolve("dir1/file-1-2.cpp");
        fileD1F3 = start.resolve("dir1/file-1-3.c");
        fileD2F1 = start.resolve(".dir2/file-2-1.js");
        fileD2F2 = start.resolve(".dir2/file-2-2.rb");
        fileD2F3 = start.resolve(".dir2/file-2-3.cpp");
        fileD3F1 = start.resolve("dir1/dir3/file-3-1.ts");
        fileD3F2 = start.resolve("dir1/dir3/file-3-2.py");

        countsD0F1 = Map.of(Java, new Stats(26, 9, 9));
        countsD0F2 = Map.of(Html, new Stats(19, 7, 4), Css, new Stats(3, 5, 0), JavaScript, new Stats(1, 6, 0));
        countsD1F1 = Map.of(Java, new Stats(26, 9, 9));
        countsD1F2 = Map.of(Cpp, new Stats(37, 2, 6));
        countsD1F3 = Map.of(C, new Stats(33, 7, 8));
        countsD2F1 = Map.of(JavaScript, new Stats(14, 11, 6));
        countsD2F2 = Map.of(Ruby, new Stats(9, 7, 3));
        countsD2F3 = Map.of(Cpp, new Stats(37, 2, 6));
        countsD3F1 = Map.of(TypeScript, new Stats(20, 9, 3));
        countsD3F2A = Map.of(Python, new Stats(5, 6, 1));
        countsD3F2B = Map.of(Python, new Stats(10, 1, 1));
    }

    @Test
    public void testWalkDefault() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(start);
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).containsEntry(fileD0F1, countsD0F1)
                          .containsEntry(fileD0F2, countsD0F2)
                          .containsEntry(fileD1F1, countsD1F1)
                          .containsEntry(fileD1F2, countsD1F2)
                          .containsEntry(fileD1F3, countsD1F3)
                          .containsEntry(fileD3F1, countsD3F1)
                          .containsEntry(fileD3F2, countsD3F2A);
    }

    @Test
    public void testWalkCountDocStrings() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(start).countDocStrings(false);
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).containsEntry(fileD0F1, countsD0F1)
                          .containsEntry(fileD0F2, countsD0F2)
                          .containsEntry(fileD1F1, countsD1F1)
                          .containsEntry(fileD1F2, countsD1F2)
                          .containsEntry(fileD1F3, countsD1F3)
                          .containsEntry(fileD3F1, countsD3F1)
                          .containsEntry(fileD3F2, countsD3F2B);
    }

    @Test
    public void testWalkIncludeHidden() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(start).excludeHidden(false);
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).containsEntry(fileD0F1, countsD0F1)
                          .containsEntry(fileD0F2, countsD0F2)
                          .containsEntry(fileD1F1, countsD1F1)
                          .containsEntry(fileD1F2, countsD1F2)
                          .containsEntry(fileD1F3, countsD1F3)
                          .containsEntry(fileD2F1, countsD2F1)
                          .containsEntry(fileD2F2, countsD2F2)
                          .containsEntry(fileD2F3, countsD2F3)
                          .containsEntry(fileD3F1, countsD3F1)
                          .containsEntry(fileD3F2, countsD3F2A);
    }

    @Test
    public void testWalkMaxDepth() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(start).maxDepth(1);
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).containsEntry(fileD0F1, countsD0F1)
                          .containsEntry(fileD0F2, countsD0F2);
    }

    @Test
    public void testWalkMatch() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(start, "*.java");
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).containsEntry(fileD0F1, countsD0F1)
                          .containsEntry(fileD1F1, countsD1F1);
    }
}
