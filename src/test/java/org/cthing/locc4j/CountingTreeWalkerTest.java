/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
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
        final Map<Path, Map<Language, Counts>> counts = walker.count();
        assertThat(counts).hasSize(8)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD0F2, treeData.countsD0F2)
                          .containsEntry(treeData.fileD1F1, treeData.countsD1F1)
                          .containsEntry(treeData.fileD1F2, treeData.countsD1F2)
                          .containsEntry(treeData.fileD1F3, treeData.countsD1F3)
                          .containsEntry(treeData.fileD1F4, treeData.countsD1F4)
                          .containsEntry(treeData.fileD3F1, treeData.countsD3F1)
                          .containsEntry(treeData.fileD3F2, treeData.countsD3F2A);
    }

    @Test
    public void testWalkCountDocStrings() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).countDocStrings(false);
        final Map<Path, Map<Language, Counts>> counts = walker.count();
        assertThat(counts).hasSize(8)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD0F2, treeData.countsD0F2)
                          .containsEntry(treeData.fileD1F1, treeData.countsD1F1)
                          .containsEntry(treeData.fileD1F2, treeData.countsD1F2)
                          .containsEntry(treeData.fileD1F3, treeData.countsD1F3)
                          .containsEntry(treeData.fileD1F4, treeData.countsD1F4)
                          .containsEntry(treeData.fileD3F1, treeData.countsD3F1)
                          .containsEntry(treeData.fileD3F2, treeData.countsD3F2B);
    }

    @Test
    public void testWalkIncludeHidden() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).excludeHidden(false);
        final Map<Path, Map<Language, Counts>> counts = walker.count();
        assertThat(counts).hasSize(11)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD0F2, treeData.countsD0F2)
                          .containsEntry(treeData.fileD1F1, treeData.countsD1F1)
                          .containsEntry(treeData.fileD1F2, treeData.countsD1F2)
                          .containsEntry(treeData.fileD1F3, treeData.countsD1F3)
                          .containsEntry(treeData.fileD1F4, treeData.countsD1F4)
                          .containsEntry(treeData.fileD2F1, treeData.countsD2F1)
                          .containsEntry(treeData.fileD2F2, treeData.countsD2F2)
                          .containsEntry(treeData.fileD2F3, treeData.countsD2F3)
                          .containsEntry(treeData.fileD3F1, treeData.countsD3F1)
                          .containsEntry(treeData.fileD3F2, treeData.countsD3F2A);
    }

    @Test
    public void testWalkMaxDepth() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start).maxDepth(1);
        final Map<Path, Map<Language, Counts>> counts = walker.count();
        assertThat(counts).hasSize(2)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD0F2, treeData.countsD0F2);
    }

    @Test
    public void testWalkPatternMatch() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start, "*.java");
        final Map<Path, Map<Language, Counts>> counts = walker.count();
        assertThat(counts).hasSize(2)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD1F1, treeData.countsD1F1);
    }

    @Test
    public void testWalkLanguageMatch() throws IOException {
        final CountingTreeWalker walker = new CountingTreeWalker(treeData.start, Language.Java);
        final Map<Path, Map<Language, Counts>> counts = walker.count();
        assertThat(counts).hasSize(2)
                          .containsEntry(treeData.fileD0F1, treeData.countsD0F1)
                          .containsEntry(treeData.fileD1F1, treeData.countsD1F1);
    }
}
