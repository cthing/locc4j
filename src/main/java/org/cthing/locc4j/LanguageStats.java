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

import javax.annotation.concurrent.ThreadSafe;


/**
 * Counts for a specific language.
 */
@ThreadSafe
@SuppressWarnings("UnusedReturnValue")
public class LanguageStats {

    /** Number of lines containing only whitespace. */
    public int blankLines;

    /** Number of lines containing code. */
    public int codeLines;

    /** Number of lines containing only comments. */
    public int commentLines;

    public final Language language;

    /**
     * Constructs the counts pertaining to the specified language.
     *
     * @param language Language to which the counts apply.
     */
    public LanguageStats(final Language language) {
        this.language = language;
    }

    /**
     * Obtains the total number of lines (code, comment and blank).
     *
     * @return Total number of lines.
     */
    public int getTotalLines() {
        return this.codeLines + this.commentLines + this.blankLines;
    }

    @Override
    public String toString() {
        return this.language.getName()
                + ": code=" + this.codeLines
                + ", comments=" + this.commentLines
                + ", blanks=" + this.blankLines;
    }
}
