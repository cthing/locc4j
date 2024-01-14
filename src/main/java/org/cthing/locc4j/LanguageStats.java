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

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;


/**
 * Counts for a specific language.
 */
@ThreadSafe
@SuppressWarnings("UnusedReturnValue")
public class LanguageStats {

    /** Number of lines containing only whitespace. */
    public final AtomicInteger blankLines;

    /** Number of lines containing code. */
    public final AtomicInteger codeLines;

    /** Number of lines containing only comments. */
    public final AtomicInteger commentLines;

    private final Language language;

    /**
     * Constructs the counts pertaining to the specified language.
     *
     * @param language Language to which the counts apply.
     */
    public LanguageStats(final Language language) {
        this.blankLines = new AtomicInteger(0);
        this.codeLines = new AtomicInteger(0);
        this.commentLines = new AtomicInteger(0);
        this.language = language;
    }

    /**
     * Obtains the language to which the counts apply.
     *
     * @return Language to which the counts apply.
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * Obtains the number of blank lines.
     *
     * @return Number of blank lines.
     */
    public int getBlankLines() {
        return this.blankLines.get();
    }

    /**
     * Obtains the number of lines containing code.
     *
     * @return Number of lines containing code.
     */
    public int getCodeLines() {
        return this.codeLines.get();
    }

    /**
     * Obtains the number of lines containing only comments.
     *
     * @return Number of liens containing only comments.
     */
    public int getCommentLines() {
        return this.commentLines.get();
    }

    /**
     * Obtains the total number of lines (code, comment and blank).
     *
     * @return Total number of lines.
     */
    public int getTotalLines() {
        return this.codeLines.get() + this.commentLines.get() + this.blankLines.get();
    }

    /**
     * Increments the number of blank lines.
     *
     * @return Incremented number of blank lines.
     */
    int incrementBlankLines() {
        return this.blankLines.incrementAndGet();
    }

    /**
     * Increments the number of code lines.
     *
     * @return Incremented number of code lines.
     */
    int incrementCodeLines() {
        return this.codeLines.incrementAndGet();
    }

    /**
     * Increments the number of comment lines.
     *
     * @return Incremented number of code lines.
     */
    int incrementCommentLines() {
        return this.commentLines.incrementAndGet();
    }

    /**
     * Adds the specified number of lines to the number of blank lines.
     *
     * @param numLines Number of lines to add
     * @return Number of blank lines after the addition.
     */
    int addBlankLines(final int numLines) {
        return this.blankLines.addAndGet(numLines);
    }

    /**
     * Adds the specified number of lines to the number of code lines.
     *
     * @param numLines Number of lines to add
     * @return Number of code lines after the addition.
     */
    int addCodeLines(final int numLines) {
        return this.codeLines.addAndGet(numLines);
    }

    /**
     * Adds the specified number of lines to the number of comment lines.
     *
     * @param numLines Number of lines to add
     * @return Number of comment lines after the addition.
     */
    int addCommentLines(final int numLines) {
        return this.commentLines.addAndGet(numLines);
    }

    @Override
    public String toString() {
        return this.language.getName()
                + ": code=" + this.codeLines
                + ", comments=" + this.commentLines
                + ", blanks=" + this.blankLines;
    }
}
