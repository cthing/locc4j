/*
 * Copyright 2023 C Thing Software
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


public class LanguageStatsTest {

    private final LanguageStats stats = new LanguageStats(Language.Css);

    @Test
    public void testConstruct() {
        assertThat(this.stats.getLanguage()).isEqualTo(Language.Css);
        assertThat(this.stats.getBlankLines()).isZero();
        assertThat(this.stats.getCodeLines()).isZero();
        assertThat(this.stats.getCommentLines()).isZero();
        assertThat(this.stats.getTotalLines()).isZero();
    }

    @Test
    public void testIncrement() {
        assertThat(this.stats.incrementBlankLines()).isEqualTo(1);

        assertThat(this.stats.incrementCodeLines()).isEqualTo(1);
        assertThat(this.stats.incrementCodeLines()).isEqualTo(2);

        assertThat(this.stats.incrementCommentLines()).isEqualTo(1);
        assertThat(this.stats.incrementCommentLines()).isEqualTo(2);
        assertThat(this.stats.incrementCommentLines()).isEqualTo(3);

        assertThat(this.stats.getBlankLines()).isEqualTo(1);
        assertThat(this.stats.getCodeLines()).isEqualTo(2);
        assertThat(this.stats.getCommentLines()).isEqualTo(3);
        assertThat(this.stats.getTotalLines()).isEqualTo(6);
    }

    @Test
    public void testAdd() {
        assertThat(this.stats.addBlankLines(2)).isEqualTo(2);
        assertThat(this.stats.addCodeLines(3)).isEqualTo(3);
        assertThat(this.stats.addCommentLines(4)).isEqualTo(4);

        assertThat(this.stats.getBlankLines()).isEqualTo(2);
        assertThat(this.stats.getCodeLines()).isEqualTo(3);
        assertThat(this.stats.getCommentLines()).isEqualTo(4);
    }

    @Test
    public void testToString() {
        this.stats.addBlankLines(2);
        this.stats.addCodeLines(3);
        this.stats.addCommentLines(4);
        assertThat(this.stats).hasToString("CSS: code=3, comments=4, blanks=2");
    }
}
