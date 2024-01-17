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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class StatsTest {

    private final Stats stats = new Stats();

    @Test
    public void testConstruct() {
        assertThat(this.stats.getBlankLines()).isZero();
        assertThat(this.stats.getCodeLines()).isZero();
        assertThat(this.stats.getCommentLines()).isZero();
        assertThat(this.stats.getTotalLines()).isZero();
    }

    @Test
    public void testAdd() {
        this.stats.codeLines = 2;
        this.stats.commentLines = 5;
        this.stats.blankLines = 3;

        final Stats stats2 = new Stats();
        stats2.codeLines = 10;
        stats2.commentLines = 21;
        stats2.blankLines = 60;
        stats2.add(this.stats);

        assertThat(stats2.getCodeLines()).isEqualTo(12);
        assertThat(stats2.getCommentLines()).isEqualTo(26);
        assertThat(stats2.getBlankLines()).isEqualTo(63);
    }

    @Test
    public void testToString() {
        this.stats.blankLines = 2;
        this.stats.codeLines = 3;
        this.stats.commentLines = 4;
        assertThat(this.stats).hasToString("[code=3, comments=4, blanks=2]");
    }
}
