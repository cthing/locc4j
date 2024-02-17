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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;


public class CountingTreeWalkerTest {

    @RegisterExtension
    static TreeDataExtension treeData = new TreeDataExtension();

    @Test
    public void testWalkDefault() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start);
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).hasSize(7)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD0F2, treeData.countsD0F2)
                          .containsEntry(treeData.fileD1F1, treeData.countsD1F1)
                          .containsEntry(treeData.fileD1F2, treeData.countsD1F2)
                          .containsEntry(treeData.fileD1F3, treeData.countsD1F3)
                          .containsEntry(treeData.fileD3F1, treeData.countsD3F1)
                          .containsEntry(treeData.fileD3F2, treeData.countsD3F2A);
    }

    @Test
    public void testWalkCountDocStrings() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).countDocStrings(false);
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).hasSize(7)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD0F2, treeData.countsD0F2)
                          .containsEntry(treeData.fileD1F1, treeData.countsD1F1)
                          .containsEntry(treeData.fileD1F2, treeData.countsD1F2)
                          .containsEntry(treeData.fileD1F3, treeData.countsD1F3)
                          .containsEntry(treeData.fileD3F1, treeData.countsD3F1)
                          .containsEntry(treeData.fileD3F2, treeData.countsD3F2B);
    }

    @Test
    public void testWalkIncludeHidden() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).excludeHidden(false);
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).hasSize(10)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD0F2, treeData.countsD0F2)
                          .containsEntry(treeData.fileD1F1, treeData.countsD1F1)
                          .containsEntry(treeData.fileD1F2, treeData.countsD1F2)
                          .containsEntry(treeData.fileD1F3, treeData.countsD1F3)
                          .containsEntry(treeData.fileD2F1, treeData.countsD2F1)
                          .containsEntry(treeData.fileD2F2, treeData.countsD2F2)
                          .containsEntry(treeData.fileD2F3, treeData.countsD2F3)
                          .containsEntry(treeData.fileD3F1, treeData.countsD3F1)
                          .containsEntry(treeData.fileD3F2, treeData.countsD3F2A);
    }

    @Test
    public void testWalkMaxDepth() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).maxDepth(1);
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).hasSize(2)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD0F2, treeData.countsD0F2);
    }

    @Test
    public void testWalkMatch() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start, "*.java");
        final Map<Path, Map<Language, Stats>> counts = walker.count();
        assertThat(counts).hasSize(2)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD1F1, treeData.countsD1F1);
    }
}
