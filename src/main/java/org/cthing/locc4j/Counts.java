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


import java.util.Objects;


/**
 * Line counts.
 */
public class Counts {

    /** A count object containing all zeros. */
    public static final Counts ZERO = new Counts(0, 0, 0);

    int codeLines;
    int commentLines;
    int blankLines;

    Counts() {
    }

    Counts(final int codeLines, final int commentLines, final int blankLines) {
        this.blankLines = blankLines;
        this.codeLines = codeLines;
        this.commentLines = commentLines;
    }

    /**
     * Obtains the number of lines of code. Code is defined as source code, markup tags, and textual content.
     *
     * @return Number of lines of code.
     */
    public int getCodeLines() {
        return this.codeLines;
    }

    /**
     * Obtains the numer of line consisting solely of comments. Note that code with trailing comments is counted
     * as code not comments.
     *
     * @return Number of lines containing only comments.
     */
    public int getCommentLines() {
        return this.commentLines;
    }

    /**
     * Obtains the number of lines containing only whitespace.
     *
     * @return Number of whitespace only lines.
     */
    public int getBlankLines() {
        return this.blankLines;
    }

    /**
     * Obtains the total number of lines (code, comment and blank).
     *
     * @return Total number of lines.
     */
    public int getTotalLines() {
        return this.codeLines + this.commentLines + this.blankLines;
    }

    /**
     * Adds the counts from the specified counts to this counts object.
     *
     * @param other Counts to be added to this counts object.
     */
    void add(final Counts other) {
        this.codeLines += other.codeLines;
        this.commentLines += other.commentLines;
        this.blankLines += other.blankLines;
    }

    @Override
    public String toString() {
        return "[code=" + this.codeLines + ", comments=" + this.commentLines + ", blanks=" + this.blankLines + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Counts counts = (Counts)obj;
        return this.codeLines == counts.codeLines
                && this.commentLines == counts.commentLines
                && this.blankLines == counts.blankLines;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.codeLines, this.commentLines, this.blankLines);
    }
}
