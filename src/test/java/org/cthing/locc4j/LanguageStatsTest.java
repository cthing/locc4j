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
        assertThat(this.stats.language).isEqualTo(Language.Css);
        assertThat(this.stats.blankLines).isZero();
        assertThat(this.stats.codeLines).isZero();
        assertThat(this.stats.commentLines).isZero();
        assertThat(this.stats.getTotalLines()).isZero();
    }

    @Test
    public void testToString() {
        this.stats.blankLines = 2;
        this.stats.codeLines = 3;
        this.stats.commentLines = 4;
        assertThat(this.stats).hasToString("CSS: code=3, comments=4, blanks=2");
    }
}
