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


/**
 * Line counts.
 */
public class Stats {

    int blankLines;
    int codeLines;
    int commentLines;

    /**
     * Obtains the number of lines containing only whitespace.
     *
     * @return Number of whitespace only lines.
     */
    public int getBlankLines() {
        return this.blankLines;
    }

    /**
     * Obtains the number of lines of code.
     *
     * @return Number of lines of code.
     */
    public int getCodeLines() {
        return this.codeLines;
    }

    /**
     * Obtains the numer of line consisting solely of comments.
     *
     * @return Number of lines containing only comments.
     */
    public int getCommentLines() {
        return this.commentLines;
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
     * Adds the counts from the specified stats to this stats object.
     *
     * @param other Stats to be added to this stats object.
     */
    void add(final Stats other) {
        this.codeLines += other.codeLines;
        this.commentLines += other.commentLines;
        this.blankLines += other.blankLines;
    }

    @Override
    public String toString() {
        return "[code=" + this.codeLines + ", comments=" + this.commentLines + ", blanks=" + this.blankLines + "]";
    }
}