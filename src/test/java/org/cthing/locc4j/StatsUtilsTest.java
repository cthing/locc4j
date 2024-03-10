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
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cthing.locc4j.Language.*;


public class StatsUtilsTest {

    @RegisterExtension
    static TreeDataExtension treeData = new TreeDataExtension();

    @Test
    public void testByLanguage() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).excludeHidden(false);
        final Map<Language, Stats> stats = StatsUtils.byLanguage(walker.count());
        assertThat(stats).hasSize(9)
                         .containsEntry(C, new Stats(33, 7, 8))
                         .containsEntry(Cpp, new Stats(74, 4, 12))
                         .containsEntry(Css, new Stats(3, 5, 0))
                         .containsEntry(Html, new Stats(19, 7, 4))
                         .containsEntry(Java, new Stats(52, 18, 18))
                         .containsEntry(JavaScript, new Stats(15, 17, 6))
                         .containsEntry(TypeScript, new Stats(20, 9, 3))
                         .containsEntry(Python, new Stats(5, 6, 1))
                         .containsEntry(Ruby, new Stats(9, 7, 3));
    }

    @Test
    public void testByLanguageGroupedFile() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).excludeHidden(false);
        final Map<Language, Set<Path>> groups = StatsUtils.byLanguageGroupedFile(walker.count());
        assertThat(groups).hasSize(9)
                          .containsEntry(C, Set.of(treeData.fileD1F3))
                          .containsEntry(Cpp, Set.of(treeData.fileD1F2, treeData.fileD2F3))
                          .containsEntry(Css, Set.of(treeData.fileD0F2))
                          .containsEntry(Html, Set.of(treeData.fileD0F2))
                          .containsEntry(Java, Set.of(treeData.fileD0F1, treeData.fileD1F1))
                          .containsEntry(JavaScript, Set.of(treeData.fileD0F2, treeData.fileD2F1))
                          .containsEntry(TypeScript, Set.of(treeData.fileD3F1))
                          .containsEntry(Python, Set.of(treeData.fileD3F2))
                          .containsEntry(Ruby, Set.of(treeData.fileD2F2));
    }

    @Test
    public void testByFile() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).excludeHidden(false);
        final Map<Path, Stats> stats = StatsUtils.byFile(walker.count());
        assertThat(stats).hasSize(10)
                         .containsEntry(treeData.fileD0F1, new Stats(26, 9, 9))
                         .containsEntry(treeData.fileD0F2, new Stats(23, 18, 4))
                         .containsEntry(treeData.fileD1F1, new Stats(26, 9, 9))
                         .containsEntry(treeData.fileD1F2, new Stats(37, 2, 6))
                         .containsEntry(treeData.fileD1F3, new Stats(33, 7, 8))
                         .containsEntry(treeData.fileD2F1, new Stats(14, 11, 6))
                         .containsEntry(treeData.fileD2F2, new Stats(9, 7, 3))
                         .containsEntry(treeData.fileD2F3, new Stats(37, 2, 6))
                         .containsEntry(treeData.fileD3F1, new Stats(20, 9, 3))
                         .containsEntry(treeData.fileD3F2, new Stats(5, 6, 1));
    }

    @Test
    public void testLanguages() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).excludeHidden(false);
        final Set<Language> langs = StatsUtils.languages(walker.count());
        assertThat(langs).containsExactlyInAnyOrder(C, Cpp, Css, Html, Java, JavaScript, TypeScript, Python, Ruby);
    }

    @Test
    public void testFiles() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).excludeHidden(false);
        final Set<Path> paths = StatsUtils.files(walker.count());
        assertThat(paths).containsExactlyInAnyOrder(
                treeData.fileD0F1,
                treeData.fileD0F2,
                treeData.fileD1F1,
                treeData.fileD1F2,
                treeData.fileD1F3,
                treeData.fileD1F4,
                treeData.fileD2F1,
                treeData.fileD2F2,
                treeData.fileD2F3,
                treeData.fileD3F1,
                treeData.fileD3F2
        );
    }

    @Test
    public void testUnrecognized() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).excludeHidden(false);
        final Set<Path> paths = StatsUtils.unrecognized(walker.count());
        assertThat(paths).containsExactly(
                treeData.fileD1F4
        );
    }

    @Test
    public void testTotal() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).excludeHidden(false);
        final Stats total = StatsUtils.total(walker.count());
        assertThat(total.getCodeLines()).isEqualTo(230);
        assertThat(total.getCommentLines()).isEqualTo(80);
        assertThat(total.getBlankLines()).isEqualTo(55);
        assertThat(total.getTotalLines()).isEqualTo(365);
    }
}
