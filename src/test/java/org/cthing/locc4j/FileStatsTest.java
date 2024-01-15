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

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class FileStatsTest {

    private final File file = new File("/tmp/foobar");
    private final FileStats stats = new FileStats(this.file);

    @Test
    public void testConstruct() {
        assertThat(this.stats.getFile()).isEqualTo(this.file);
        assertThat(this.stats.getStats()).isEmpty();
    }

    @Test
    public void testStats() {
        LanguageStats counts = this.stats.stats(Language.Css);
        counts.blankLines = 3;

        counts = this.stats.stats(Language.Java);
        counts.codeLines = 5;

        final Map<Language, LanguageStats> countsMap = this.stats.getStats();
        assertThat(countsMap).containsOnlyKeys(Language.Css, Language.Java);
        assertThat(countsMap.get(Language.Css).blankLines).isEqualTo(3);
        assertThat(countsMap.get(Language.Css).codeLines).isZero();
        assertThat(countsMap.get(Language.Css).commentLines).isZero();
        assertThat(countsMap.get(Language.Java).blankLines).isZero();
        assertThat(countsMap.get(Language.Java).codeLines).isEqualTo(5);
        assertThat(countsMap.get(Language.Java).commentLines).isZero();

        assertThat(this.stats).hasToString("""
                                           /tmp/foobar:
                                               CSS: code=0, comments=0, blanks=3
                                               Java: code=5, comments=0, blanks=0""");
    }
}
