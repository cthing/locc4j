/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.locc4j;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class CountsTest {

    private final Counts counts = new Counts();

    @Test
    public void testConstruct() {
        assertThat(this.counts.getBlankLines()).isZero();
        assertThat(this.counts.getCodeLines()).isZero();
        assertThat(this.counts.getCommentLines()).isZero();
        assertThat(this.counts.getTotalLines()).isZero();
    }

    @Test
    public void testZero() {
        assertThat(Counts.ZERO.getBlankLines()).isZero();
        assertThat(Counts.ZERO.getCodeLines()).isZero();
        assertThat(Counts.ZERO.getCommentLines()).isZero();
        assertThat(Counts.ZERO.getTotalLines()).isZero();
    }

    @Test
    public void testAdd() {
        this.counts.codeLines = 2;
        this.counts.commentLines = 5;
        this.counts.blankLines = 3;

        final Counts counts2 = new Counts();
        counts2.codeLines = 10;
        counts2.commentLines = 21;
        counts2.blankLines = 60;
        counts2.add(this.counts);

        assertThat(counts2.getCodeLines()).isEqualTo(12);
        assertThat(counts2.getCommentLines()).isEqualTo(26);
        assertThat(counts2.getBlankLines()).isEqualTo(63);
    }

    @Test
    public void testToString() {
        this.counts.blankLines = 2;
        this.counts.codeLines = 3;
        this.counts.commentLines = 4;
        assertThat(this.counts).hasToString("[code=3, comments=4, blanks=2]");
    }
}
