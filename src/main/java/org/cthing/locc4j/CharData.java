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
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * Represents the character data to be counted. The data is read-only and copies of the data are not made when
 * reading it or performing operations (e.g. trim). The {@link CharSequence} interface is implemented to allow
 * this class to be used with classes that implement or accept that interface.
 */
@SuppressWarnings("Convert2streamapi")
public class CharData implements CharSequence {

    private final char[] buffer;
    private final int offset;
    private final int length;

    public CharData(final char[] buffer) {
        this(buffer, 0, buffer.length);
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
     * Trims the leading and trailing whitespace from the data.
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
        return regex.matcher(this).matches();
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
