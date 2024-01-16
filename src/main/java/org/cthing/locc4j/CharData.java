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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents the character data to be counted. The data is read-only and copies of the data are not made when
 * reading it or performing operations (e.g. trim). The {@link CharSequence} interface is implemented to allow
 * this class to be used with classes that implement or accept that interface.
 */
@SuppressWarnings("Convert2streamapi")
public class CharData implements CharSequence {

    /**
     * Provides iteration over the character data by line. In addition to the line character data, the start
     * (inclusive) and end (exclusive) indices of the line are provided. The line character data includes the
     * terminating newline, if present.
     */
    public final class LineIterator implements Iterator<CharData> {

        private int start;
        private int end;

        private LineIterator() {
            this(0);
        }

        private LineIterator(final int start) {
            this.start = start;
            this.end = start;
        }

        @Override
        public boolean hasNext() {
            return this.end < CharData.this.length;
        }

        @Override
        public CharData next() {
            this.start = this.end;
            while (true) {
                if (this.end >= CharData.this.length
                        || CharData.this.buffer[CharData.this.offset + this.end++] == '\n') {
                    break;
                }
            }
            return CharData.this.subSequence(this.start, this.end);
        }

        /**
         * Obtains the index of the start of the line.
         *
         * @return Index of the start of the line, inclusive.
         */
        public int getStart() {
            return this.start;
        }

        /**
         * Obtains the index of the end of the line. The line includes the terminating newline.
         *
         * @return Index of the end of the line, exclusive.
         */
        public int getEnd() {
            return this.end;
        }

        @Override
        public String toString() {
            return "Line(" + this.start + ", " + this.end + ")";
        }
    }


    private final char[] buffer;
    private final int offset;
    private final int length;

    public CharData(final char[] buffer) {
        this(buffer, 0, buffer.length);
    }

    public CharData(final List<CharData> chunks) {
        int totalLength = 0;
        for (final CharData chunk : chunks) {
            totalLength += chunk.length;
        }

        if (totalLength == 0) {
            this.buffer = new char[0];
            this.offset = 0;
            this.length = 0;
        } else {
            this.buffer = new char[totalLength];
            this.offset = 0;
            this.length = totalLength;

            int startPos = 0;
            for (final CharData chunk : chunks) {
                System.arraycopy(chunk.buffer, chunk.offset, this.buffer, startPos, chunk.length);
                startPos += chunk.length;
            }
        }
    }

    private CharData(final char[] buffer, final int offset, final int length) {
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public boolean isEmpty() {
        return this.length == 0;
    }

    /**
     * Indicates whether the data are all whitespace or empty.
     *
     * @return {@code true} if the data is all whitespace or empty.
     */
    public boolean isBlank() {
        for (int i = 0, j = this.offset; i < this.length; i++, j++) {
            if (!Character.isWhitespace(this.buffer[j])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public char charAt(final int index) {
        return this.buffer[this.offset + Objects.checkIndex(index, this.length)];
    }

    @Override
    public CharData subSequence(final int start, final int end) {
        Objects.checkFromToIndex(start, end, this.length);
        return new CharData(this.buffer, this.offset + start, end - start);
    }

    /**
     * Obtains a character data buffer based on this buffer starting at the specified index and going to the
     * end of the buffer. This is equivalent to calling {@code buffer.subSequence(start, buffer.length())}.
     *
     * @param start Starting index for the buffer
     * @return New buffer based on this buffer at the specified stating index to the end.
     */
    public CharData subSequence(final int start) {
        return subSequence(start, this.length);
    }

    /**
     * Indicates whether the character data starts with the specified prefix.
     *
     * @param prefix Prefix to test
     * @return {@code true} if the character data starts with the specified prefix. {@code false} if it does not or
     *      the prefix is longer than the character data.
     */
    public boolean startsWith(final CharSequence prefix) {
        final int prefixLen = prefix.length();
        if (prefixLen > this.length) {
            return false;
        }

        for (int i = 0, j = this.offset; i < prefixLen; i++, j++) {
            if (prefix.charAt(i) != this.buffer[j]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indicates whether the character data ends with the specified suffix.
     *
     * @param suffix Suffix to test
     * @return {@code true} if the character data ends with the specified suffix. {@code false} if it does not or
     *      the suffix is longer than the character data.
     */
    public boolean endsWith(final CharSequence suffix) {
        final int suffixLen = suffix.length();
        if (suffixLen > this.length) {
            return false;
        }

        for (int i = 0, j = this.offset + this.length - suffixLen; i < suffixLen; i++, j++) {
            if (suffix.charAt(i) != this.buffer[j]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Obtains an iterator for traversing the character data line by line.
     *
     * @return Iterator for traversing the character data by lines.
     */
    public LineIterator lineIterator() {
        return new LineIterator();
    }

    /**
     * Counts the number of lines in the character data.
     *
     * @return Number of lines in the character data.
     */
    public int countLines() {
        if (this.length == 0) {
            return 0;
        }

        boolean endedNewline = false;
        int count = 0;
        for (int i = this.offset; i < this.length; i++) {
            endedNewline = this.buffer[i] == '\n';
            if (endedNewline) {
                count++;
            }
        }
        return endedNewline ? count : (count + 1);
    }

    /**
     * Obtains an iterator for traversing the character data line by line starting at the specified position
     * in the data.
     *
     * @param start Starting position for the iterator
     * @return Iterator for traversing the character data by lines.
     */
    public LineIterator lineIterator(final int start) {
        return new LineIterator(start);
    }

    /**
     * Finds the index of the start of the line containing the specified index. The start of a line is either the
     * index immediately following a newline or zero.
     *
     * @param index Index in the character data from which to look for the start of the line containing that index
     * @return Index of the start of the line containing the specified index.
     */
    public int findLineStart(final int index) {
        for (int i = this.offset + Objects.checkIndex(index, this.length) - 1; i >= this.offset; i--) {
            if (this.buffer[i] == '\n') {
                return i - this.offset + 1;
            }
        }
        return 0;
    }

    /**
     * Finds the index of the first occurrence of the specified character in the data.
     *
     * @param ch Character to find
     * @return Index of the character if found in the data. Returns -1 if the character is not found.
     */
    public int indexOf(final char ch) {
        return indexOf(ch, 0);
    }

    /**
     * Finds the index of the first occurrence of the specified character in the data starting at the specified index.
     *
     * @param ch Character to find
     * @param fromIndex Index from which to start looking for the character (inclusive)
     * @return Index of the character if found in the data. Returns -1 if the character is not found.
     */
    public int indexOf(final char ch, final int fromIndex) {
        Objects.checkIndex(fromIndex, this.length);
        for (int i = fromIndex, j = this.offset + fromIndex; i < this.length; i++, j++) {
            if (this.buffer[j] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the index of the first occurrence of the specified character sequence in the data.
     *
     * @param sequence Character sequence to find
     * @return Index of the character sequence if found in the data. Returns -1 if the character is not found.
     */
    public int indexOf(final CharSequence sequence) {
        return indexOf(sequence, 0);
    }

    /**
     * Finds the index of the first occurrence of the specified character sequence in the data starting at the
     * specified index.
     *
     * @param sequence Character sequence to find
     * @param fromIndex Index from which to start looking for the character sequence (inclusive)
     * @return Index of the character sequence if found in the data. Returns -1 if the character is not found.
     */
    public int indexOf(final CharSequence sequence, final int fromIndex) {
        Objects.checkIndex(fromIndex, this.length);

        final int seqLen = sequence.length();
        if (seqLen == 0) {
            return fromIndex;
        }

        final int maxLen = this.length - seqLen;
        for (int i = fromIndex; i <= maxLen; i++) {
            boolean match = true;

            for (int j = 0, k = this.offset + i; j < seqLen; j++, k++) {
                if (this.buffer[k] != sequence.charAt(j)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the index of the last occurrence of the specified character in the data.
     *
     * @param ch Character to find
     * @return Index of the character if found in the data. Returns -1 if the character is not found.
     */
    public int lastIndexOf(final char ch) {
        return lastIndexOf(ch, this.length - 1);
    }

    /**
     * Finds the index of the last occurrence of the specified character in the data starting at the specified index.
     *
     * @param ch Character to find
     * @param fromIndex Index from which to start looking for the character (inclusive)
     * @return Index of the character if found in the data. Returns -1 if the character is not found.
     */
    public int lastIndexOf(final char ch, final int fromIndex) {
        Objects.checkIndex(fromIndex, this.length);
        for (int i = fromIndex, j = this.offset + fromIndex; i >= 0; i--, j--) {
            if (this.buffer[j] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the index of the last occurrence of the specified character sequence in the data.
     *
     * @param sequence Character sequence to find
     * @return Index of the character sequence if found in the data. Returns -1 if the character is not found.
     */
    public int lastIndexOf(final CharSequence sequence) {
        return lastIndexOf(sequence, this.length - 1);
    }

    /**
     * Finds the index of the last occurrence of the specified character sequence in the data starting at the
     * specified index.
     *
     * @param sequence Character sequence to find
     * @param fromIndex Index from which to start looking for the character sequence (inclusive)
     * @return Index of the character sequence if found in the data. Returns -1 if the character is not found.
     */
    public int lastIndexOf(final CharSequence sequence, final int fromIndex) {
        Objects.checkIndex(fromIndex, this.length);

        final int seqLen = sequence.length();
        if (seqLen == 0) {
            return fromIndex;
        }

        final int startIndex = fromIndex - seqLen + 1;
        for (int i = startIndex; i >= 0; i--) {
            boolean match = true;

            for (int j = 0, k = this.offset + i; j < seqLen; j++, k++) {
                if (this.buffer[k] != sequence.charAt(j)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Trims any leading and trailing whitespace from the data.
     *
     * @return New character data buffer with leading and trailing whitespace removed.
     */
    public CharData trim() {
        int start = this.offset;
        int end = this.offset + this.length - 1;

        // Trim from the start
        while (start <= end) {
            final char ch = this.buffer[start];
            if (ch == ' ' || ch == '\t' || Character.isWhitespace(ch)) {
                start++;
            } else {
                break;
            }
        }

        // Trim from the end
        while (end >= start) {
            final char ch = this.buffer[end];
            if (ch == ' ' || ch == '\t' || Character.isWhitespace(ch)) {
                end--;
            } else {
                break;
            }
        }

        // Check if the entire string is whitespace
        if (start > end) {
            return new CharData(this.buffer, (start == 0) ? 0 : start - 1, 0);
        }

        return new CharData(this.buffer, start, end - start + 1);
    }

    /**
     * Trims any leading whitespace from the data.
     *
     * @return New character data buffer with leading whitespace removed.
     */
    public CharData trimLeading() {
        int start = this.offset;
        final int end = this.offset + this.length - 1;

        // Trim from the start
        while (start <= end) {
            final char ch = this.buffer[start];
            if (ch == ' ' || ch == '\t' || Character.isWhitespace(ch)) {
                start++;
            } else {
                break;
            }
        }

        // Check if the entire string is whitespace
        if (start > end) {
            return new CharData(this.buffer, (start == 0) ? 0 : start - 1, 0);
        }

        return new CharData(this.buffer, start, end - start + 1);
    }

    /**
     * Trims any trailing whitespace from the data.
     *
     * @return New character data buffer with trailing whitespace removed.
     */
    public CharData trimTrailing() {
        int end = this.offset + this.length - 1;

        // Trim from the end
        while (end >= this.offset) {
            final char ch = this.buffer[end];
            if (ch == ' ' || ch == '\t' || Character.isWhitespace(ch)) {
                end--;
            } else {
                break;
            }
        }

        // Check if the entire string is whitespace
        if (this.offset > end) {
            return new CharData(this.buffer, (this.offset == 0) ? 0 : this.offset - 1, 0);
        }

        return new CharData(this.buffer, this.offset, end - this.offset + 1);
    }

    /**
     * Trims any leading whitespace on the first line of the data and any trailing whitespace on the last line of
     * the data. For a single line, the results are identical to calling {@link #trim()}.
     *
     * @return New character data buffer with the whitespace trimmed off the first and last lines.
     */
    public CharData trimFirstLastLine() {
        int start = this.offset;
        int end = this.offset + this.length - 1;

        // Trim from the start to the end of the line
        while (start <= end) {
            final char ch = this.buffer[start];
            if (ch == '\n') {
                start++;
                break;
            }
            if (ch == ' ' || ch == '\t' || Character.isWhitespace(ch)) {
                start++;
            } else {
                break;
            }
        }

        // Trim from the end to the start of the last line
        while (end >= start) {
            final char ch = this.buffer[end];
            if (ch == '\n') {
                // Do not remove blank lines.
                if (end - 1 >= start && this.buffer[end - 1] != '\n') {
                    end--;
                }
                break;
            }
            if (ch == ' ' || ch == '\t' || Character.isWhitespace(ch)) {
                end--;
            } else {
                break;
            }
        }

        // Check if the entire string is whitespace
        if (start > end) {
            return new CharData(this.buffer, (start == 0) ? 0 : start - 1, 0);
        }

        return new CharData(this.buffer, start, end - start + 1);
    }

    /**
     * Trims any trailing whitespace on the last line of the data. For a single line, the results are identical to
     * calling {@link #trimTrailing()}.
     *
     * @return New character data buffer with the whitespace trimmed off the last line.
     */
    public CharData trimLastLine() {
        int end = this.offset + this.length - 1;

        // Trim from the end to the start of the last line
        while (end >= this.offset) {
            final char ch = this.buffer[end];
            if (ch == '\n') {
                end--;
                break;
            }
            if (ch == ' ' || ch == '\t' || Character.isWhitespace(ch)) {
                end--;
            } else {
                break;
            }
        }

        // Check if the entire string is whitespace
        if (this.offset > end) {
            return new CharData(this.buffer, (this.offset == 0) ? 0 : this.offset - 1, 0);
        }

        return new CharData(this.buffer, this.offset, end - this.offset + 1);
    }

    /**
     * Splits the data buffer into two data buffers at the specified index. The first buffer contains the data
     * from {@code [0, index)} and the second buffer contains the data from {@code [index, length)}.
     * <p>
     * For example:
     * </p>
     * <pre>
     *     new CharData("abcdef".toCharArray()).splitAt(0);  => [], ["abcdef"]
     *     new CharData("abcdef".toCharArray()).splitAt(6);  => ["abcdef"], []
     *     new CharData("abcdef".toCharArray()).splitAt(2);  => ["ab"}, ["cdef"]
     * </pre>
     *
     * @param index Position at which to split the buffer, exclusive
     * @return Two character data buffers split at the specified index
     */
    public CharData[] splitAt(final int index) {
        final int idx = Objects.checkIndex(index, this.length + 1);

        if (this.length == 0) {
            return new CharData[] { this, this };
        }
        if (idx == 0) {
            return new CharData[] { new CharData(this.buffer, this.offset, 0), this };
        }
        if (idx == this.length) {
            return new CharData[] { this, new CharData(this.buffer, this.offset + this.length - 1, 0) };
        }

        return new CharData[] {
                new CharData(this.buffer, this.offset, idx),
                new CharData(this.buffer, this.offset + idx, this.length - idx),
        };
    }

    /**
     * Indicates if the character data is equal to the specified sequence.
     *
     * @param sequence Character sequence to test
     * @return {@code true} if the character data is equal to the specified sequence.
     */
    public boolean contentEquals(final CharSequence sequence) {
        if (this.length != sequence.length()) {
            return false;
        }
        for (int i = 0, j = this.offset; i < this.length; i++, j++) {
            if (this.buffer[j] != sequence.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Indicates whether the character data matches the specified regular expression.
     *
     * @param regex Regular expression to match
     * @return {@code true} if the character data matches the specified regular expression.
     */
    public boolean matches(final Pattern regex) {
        return matcher(regex).matches();
    }

    /**
     * Creates a matcher for the specified regular expression against the character data.
     *
     * @param regex Regular expression to create the matcher
     * @return Matcher for the specified regular expression against the character data.
     */
    public Matcher matcher(final Pattern regex) {
        return regex.matcher(this);
    }

    /**
     * Indicates if the character data contains the specified sequence.
     *
     * @param sequence Character sequence to test
     * @return {@code true} if the character data contains the specified sequence.
     */
    public boolean contains(final CharSequence sequence) {
        final int seqLen = sequence.length();
        if (seqLen == 0) {
            return true;
        }

        final int maxLen = this.length - seqLen;
        for (int i = 0; i <= maxLen; i++) {
            boolean match = true;

            for (int j = 0, k = this.offset + i; j < seqLen; j++, k++) {
                if (this.buffer[k] != sequence.charAt(j)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return new String(this.buffer, this.offset, this.length);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final CharData charData = (CharData)obj;
        return this.offset == charData.offset
                && this.length == charData.length
                && Arrays.equals(this.buffer, charData.buffer);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(this.offset, this.length) + Arrays.hashCode(this.buffer);
    }
}
