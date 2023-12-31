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

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIndexOutOfBoundsException;


public class CharDataTest {

    @Test
    public void testProperties() {
        final String data = "hello world";
        final CharData buffer = new CharData(data.toCharArray());
        assertThat(buffer.isEmpty()).isFalse();
        assertThat(buffer.isBlank()).isFalse();
        assertThat(buffer.length()).isEqualTo(data.length());
        assertThat(buffer).hasToString(data);
    }

    @Test
    public void testCharAt() {
        final String data = "hello";
        final CharData buffer = new CharData(data.toCharArray());
        assertThat(buffer.charAt(0)).isEqualTo('h');
        assertThat(buffer.charAt(1)).isEqualTo('e');
        assertThat(buffer.charAt(2)).isEqualTo('l');
        assertThat(buffer.charAt(3)).isEqualTo('l');
        assertThat(buffer.charAt(4)).isEqualTo('o');
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer.charAt(-1));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer.charAt(6));
    }

    @Test
    public void testEmpty() {
        final CharData buffer = new CharData(new char[0]);
        assertThat(buffer.isEmpty()).isTrue();
        assertThat(buffer.isBlank()).isTrue();
        assertThat(buffer.length()).isZero();
        assertThat(buffer).hasToString("");

        final CharSequence buffer2 = buffer.subSequence(0, 0);
        assertThat(buffer2.isEmpty()).isTrue();
        assertThat(buffer2.length()).isZero();
        assertThat(buffer2).hasToString("");
    }

    @Test
    public void testBlank() {
        assertThat(new CharData("".toCharArray()).isBlank()).isTrue();
        assertThat(new CharData(" ".toCharArray()).isBlank()).isTrue();
        assertThat(new CharData("  \t\n".toCharArray()).isBlank()).isTrue();
        assertThat(new CharData("  \t\nHello".toCharArray()).isBlank()).isFalse();

        final CharData buffer = new CharData("        t".toCharArray());
        final CharData buffer2 = buffer.subSequence(6, 9);
        assertThat(buffer2.isBlank()).isFalse();
        final CharData buffer3 = buffer.subSequence(6, 8);
        assertThat(buffer3.isBlank()).isTrue();
    }

    @Test
    public void testSubSequenceStartEnd() {
        final String data = "hello you";
        final CharData buffer = new CharData(data.toCharArray());

        final CharSequence buffer1 = buffer.subSequence(0, 9);
        assertThat(buffer1.isEmpty()).isFalse();
        assertThat(buffer1.length()).isEqualTo(9);
        assertThat(buffer1.charAt(0)).isEqualTo('h');
        assertThat(buffer1.charAt(4)).isEqualTo('o');
        assertThat(buffer1.charAt(8)).isEqualTo('u');
        assertThat(buffer1).hasToString("hello you");

        final CharSequence buffer2 = buffer.subSequence(0, 5);
        assertThat(buffer2.isEmpty()).isFalse();
        assertThat(buffer2.length()).isEqualTo(5);
        assertThat(buffer2.charAt(0)).isEqualTo('h');
        assertThat(buffer2.charAt(4)).isEqualTo('o');
        assertThat(buffer2).hasToString("hello");

        final CharSequence buffer3 = buffer.subSequence(6, 9);
        assertThat(buffer3.isEmpty()).isFalse();
        assertThat(buffer3.length()).isEqualTo(3);
        assertThat(buffer3.charAt(0)).isEqualTo('y');
        assertThat(buffer3.charAt(2)).isEqualTo('u');
        assertThat(buffer3).hasToString("you");

        final CharSequence buffer4 = buffer2.subSequence(1, 4);
        assertThat(buffer4.isEmpty()).isFalse();
        assertThat(buffer4.length()).isEqualTo(3);
        assertThat(buffer4.charAt(0)).isEqualTo('e');
        assertThat(buffer4.charAt(2)).isEqualTo('l');
        assertThat(buffer4).hasToString("ell");

        final CharSequence buffer5 = buffer3.subSequence(1, 3);
        assertThat(buffer5.isEmpty()).isFalse();
        assertThat(buffer5.length()).isEqualTo(2);
        assertThat(buffer5.charAt(0)).isEqualTo('o');
        assertThat(buffer5.charAt(1)).isEqualTo('u');
        assertThat(buffer5).hasToString("ou");

        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer.subSequence(-1, 2));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer.subSequence(0, -2));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer.subSequence(0, 10));
    }

    @Test
    public void testSubSequenceStart() {
        final String data = "hello you";
        final CharData buffer = new CharData(data.toCharArray());

        final CharSequence buffer2 = buffer.subSequence(0);
        assertThat(buffer2.isEmpty()).isFalse();
        assertThat(buffer2.length()).isEqualTo(9);
        assertThat(buffer2.charAt(0)).isEqualTo('h');
        assertThat(buffer2.charAt(4)).isEqualTo('o');
        assertThat(buffer2.charAt(8)).isEqualTo('u');
        assertThat(buffer2).hasToString("hello you");

        final CharSequence buffer3 = buffer.subSequence(6);
        assertThat(buffer3.isEmpty()).isFalse();
        assertThat(buffer3.length()).isEqualTo(3);
        assertThat(buffer3.charAt(0)).isEqualTo('y');
        assertThat(buffer3.charAt(2)).isEqualTo('u');
        assertThat(buffer3).hasToString("you");

        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer.subSequence(-1));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer.subSequence(10));
    }

    @Test
    public void testStartsWith() {
        final CharData buffer = new CharData("Hello World".toCharArray());
        assertThat(buffer.startsWith("Hel")).isTrue();
        assertThat(buffer.startsWith("Wor")).isFalse();
        assertThat(buffer.startsWith("")).isTrue();
        assertThat(buffer.startsWith("Hello World")).isTrue();
        assertThat(buffer.startsWith("Hello World One")).isFalse();

        final CharData buffer2 = buffer.subSequence(1, 4);
        assertThat(buffer2.startsWith("ell")).isTrue();
    }

    @Test
    public void testTrim() {
        CharData buffer = new CharData("    ".toCharArray());
        assertThat(buffer.trim().toString()).isEmpty();

        buffer = new CharData("".toCharArray());
        assertThat(buffer.trim().toString()).isEmpty();

        buffer = new CharData("  hello\t".toCharArray());
        assertThat(buffer.trim()).hasToString("hello");

        buffer = new CharData("  hello".toCharArray());
        assertThat(buffer.trim()).hasToString("hello");

        buffer = new CharData("hello    ".toCharArray());
        assertThat(buffer.trim()).hasToString("hello");

        buffer = new CharData("   hello    ".toCharArray());
        CharData buffer2 = buffer.subSequence(1, 9);
        assertThat(buffer2.trim()).hasToString("hello");
        buffer2 = buffer.subSequence(11, 12);
        assertThat(buffer2.trim().toString()).isEmpty();
        buffer2 = buffer.subSequence(0, 1);
        assertThat(buffer2.trim().toString()).isEmpty();
    }

    @Test
    public void testSplitAt() {
        final CharData buffer1 = new CharData("abcdef".toCharArray());
        CharData[] result = buffer1.splitAt(0);
        assertThat(result[0]).hasToString("");
        assertThat(result[1]).hasToString("abcdef");
        result = buffer1.splitAt(6);
        assertThat(result[0]).hasToString("abcdef");
        assertThat(result[1]).hasToString("");
        result = buffer1.splitAt(2);
        assertThat(result[0]).hasToString("ab");
        assertThat(result[1]).hasToString("cdef");

        final CharData[] result2 = result[1].splitAt(1);
        assertThat(result2[0]).hasToString("c");
        assertThat(result2[1]).hasToString("def");

        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.splitAt(-1));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.splitAt(7));

        final CharData buffer3 = new CharData("".toCharArray());
        result = buffer3.splitAt(0);
        assertThat(result[0]).hasToString("");
        assertThat(result[1]).hasToString("");
    }

    @Test
    public void testContentEquals() {
        final CharData buffer1 = new CharData("hello world".toCharArray());
        assertThat(buffer1.contentEquals("hello world")).isTrue();
        assertThat(buffer1.contentEquals("world hello")).isFalse();
        assertThat(buffer1.contentEquals("hello")).isFalse();
        assertThat(buffer1.contentEquals("")).isFalse();

        final CharData buffer2 = new CharData("".toCharArray());
        assertThat(buffer2.contentEquals("hello world")).isFalse();
        assertThat(buffer2.contentEquals("hello")).isFalse();
        assertThat(buffer2.contentEquals("")).isTrue();

        final CharData buffer3 = buffer1.subSequence(2, 7);
        assertThat(buffer3.contentEquals("llo w")).isTrue();
        assertThat(buffer3.contentEquals("w llo")).isFalse();
        assertThat(buffer3.contentEquals("hello world")).isFalse();
        assertThat(buffer3.contentEquals("")).isFalse();
    }

    @Test
    public void testContains() {
        final CharData buffer1 = new CharData("hello world".toCharArray());
        assertThat(buffer1.contains("hello world")).isTrue();
        assertThat(buffer1.contains("hello")).isTrue();
        assertThat(buffer1.contains("world")).isTrue();
        assertThat(buffer1.contains("abcd")).isFalse();
        assertThat(buffer1.contains("this is a test of something long")).isFalse();
        assertThat(buffer1.contains("")).isTrue();

        final CharData buffer2 = new CharData("".toCharArray());
        assertThat(buffer2.contains("hello world")).isFalse();
        assertThat(buffer2.contains("")).isTrue();

        final CharData buffer3 = buffer1.subSequence(2, 7);
        assertThat(buffer3.contains("llo w")).isTrue();
        assertThat(buffer3.contains("lo ")).isTrue();
        assertThat(buffer3.contains("")).isTrue();
    }

    @Test
    public void testIndexOfChar() {
        final CharData buffer1 = new CharData("hello world".toCharArray());
        assertThat(buffer1.indexOf('h')).isEqualTo(0);
        assertThat(buffer1.indexOf('o')).isEqualTo(4);
        assertThat(buffer1.indexOf('d')).isEqualTo(10);
        assertThat(buffer1.indexOf('z')).isEqualTo(-1);
        assertThat(buffer1.indexOf('o', 5)).isEqualTo(7);
        assertThat(buffer1.indexOf('o', 7)).isEqualTo(7);
        assertThat(buffer1.indexOf('o', 8)).isEqualTo(-1);
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.indexOf('o', -1));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.indexOf('o', 11));

        final CharData buffer2 = buffer1.subSequence(6, 10);
        assertThat(buffer2.indexOf('w')).isEqualTo(0);
        assertThat(buffer2.indexOf('l')).isEqualTo(3);
        assertThat(buffer2.indexOf('r', 1)).isEqualTo(2);
        assertThat(buffer2.indexOf('r', 2)).isEqualTo(2);
        assertThat(buffer2.indexOf('r', 3)).isEqualTo(-1);
    }

    @Test
    public void testIndexOfSequence() {
        final CharData buffer1 = new CharData("hello world".toCharArray());
        assertThat(buffer1.indexOf("")).isEqualTo(0);
        assertThat(buffer1.indexOf("h")).isEqualTo(0);
        assertThat(buffer1.indexOf("hello")).isEqualTo(0);
        assertThat(buffer1.indexOf("llo")).isEqualTo(2);
        assertThat(buffer1.indexOf("world")).isEqualTo(6);
        assertThat(buffer1.indexOf("abcd")).isEqualTo(-1);
        assertThat(buffer1.indexOf("abcdefghijklmnop")).isEqualTo(-1);
        assertThat(buffer1.indexOf("", 2)).isEqualTo(2);
        assertThat(buffer1.indexOf("o w", 2)).isEqualTo(4);
        assertThat(buffer1.indexOf("or", 5)).isEqualTo(7);
        assertThat(buffer1.indexOf("or", 7)).isEqualTo(7);
        assertThat(buffer1.indexOf("or", 8)).isEqualTo(-1);
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.indexOf("o", -1));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.indexOf("o", 11));

        final CharData buffer2 = buffer1.subSequence(6, 10);
        assertThat(buffer2.indexOf("wo")).isEqualTo(0);
        assertThat(buffer2.indexOf("rl")).isEqualTo(2);
        assertThat(buffer2.indexOf("or")).isEqualTo(1);
        assertThat(buffer2.indexOf("abc")).isEqualTo(-1);
        assertThat(buffer2.indexOf("abcedfghijk")).isEqualTo(-1);
        assertThat(buffer2.indexOf("rl", 1)).isEqualTo(2);
        assertThat(buffer2.indexOf("rl", 2)).isEqualTo(2);
        assertThat(buffer2.indexOf("rl", 3)).isEqualTo(-1);
    }

    @Test
    public void testLastIndexOfChar() {
        final CharData buffer1 = new CharData("hello world".toCharArray());
        assertThat(buffer1.lastIndexOf('h')).isEqualTo(0);
        assertThat(buffer1.lastIndexOf('o')).isEqualTo(7);
        assertThat(buffer1.lastIndexOf('d')).isEqualTo(10);
        assertThat(buffer1.lastIndexOf('z')).isEqualTo(-1);
        assertThat(buffer1.lastIndexOf('o', 3)).isEqualTo(-1);
        assertThat(buffer1.lastIndexOf('o', 4)).isEqualTo(4);
        assertThat(buffer1.lastIndexOf('o', 7)).isEqualTo(7);
        assertThat(buffer1.lastIndexOf('o', 8)).isEqualTo(7);
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.lastIndexOf('o', -1));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.lastIndexOf('o', 11));

        final CharData buffer2 = buffer1.subSequence(4, 10);
        assertThat(buffer2.lastIndexOf('w')).isEqualTo(2);
        assertThat(buffer2.lastIndexOf('l')).isEqualTo(5);
        assertThat(buffer2.lastIndexOf('o')).isEqualTo(3);
        assertThat(buffer2.lastIndexOf('o', 1)).isEqualTo(0);
        assertThat(buffer2.lastIndexOf('o', 2)).isEqualTo(0);
        assertThat(buffer2.lastIndexOf('o', 4)).isEqualTo(3);
        assertThat(buffer2.lastIndexOf('l', 4)).isEqualTo(-1);
    }

    @Test
    public void testLastIndexOfSequence() {
        final CharData buffer1 = new CharData("hello world".toCharArray());
        assertThat(buffer1.lastIndexOf("")).isEqualTo(10);
        assertThat(buffer1.lastIndexOf("h")).isEqualTo(0);
        assertThat(buffer1.lastIndexOf("hello")).isEqualTo(0);
        assertThat(buffer1.lastIndexOf("l")).isEqualTo(9);
        assertThat(buffer1.lastIndexOf("world")).isEqualTo(6);
        assertThat(buffer1.lastIndexOf("abcd")).isEqualTo(-1);
        assertThat(buffer1.lastIndexOf("abcdefghijklmnop")).isEqualTo(-1);
        assertThat(buffer1.lastIndexOf("", 2)).isEqualTo(2);
        assertThat(buffer1.lastIndexOf("or", 4)).isEqualTo(-1);
        assertThat(buffer1.lastIndexOf("or", 5)).isEqualTo(-1);
        assertThat(buffer1.lastIndexOf("or", 8)).isEqualTo(7);
        assertThat(buffer1.lastIndexOf("or", 9)).isEqualTo(7);
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.lastIndexOf("o", -1));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> buffer1.lastIndexOf("o", 11));

        final CharData buffer2 = buffer1.subSequence(6, 10);
        assertThat(buffer2.lastIndexOf("wo")).isEqualTo(0);
        assertThat(buffer2.lastIndexOf("rl")).isEqualTo(2);
        assertThat(buffer2.lastIndexOf("or")).isEqualTo(1);
        assertThat(buffer2.lastIndexOf("abc")).isEqualTo(-1);
        assertThat(buffer2.lastIndexOf("abcedfghijk")).isEqualTo(-1);
        assertThat(buffer2.lastIndexOf("rl", 1)).isEqualTo(-1);
        assertThat(buffer2.lastIndexOf("rl", 2)).isEqualTo(-1);
        assertThat(buffer2.lastIndexOf("rl", 3)).isEqualTo(2);
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(CharData.class)
                      .usingGetClass()
                      .verify();
    }
}
