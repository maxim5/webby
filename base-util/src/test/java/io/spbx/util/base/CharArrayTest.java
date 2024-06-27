package io.spbx.util.base;

import org.junit.jupiter.api.Test;

import java.nio.CharBuffer;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.MoreTruth.assertAlso;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CharArrayTest {
    @Test
    public void create_empty_string() {
        CharArray array = new CharArray("");
        assertThat(array.start()).isEqualTo(0);
        assertThat(array.end()).isEqualTo(0);
        assertThat(array.length()).isEqualTo(0);
    }

    @Test
    public void create_empty_same_pointers() {
        CharArray array = new CharArray("foo", 3, 3);
        assertThat(array.start()).isEqualTo(3);
        assertThat(array.end()).isEqualTo(3);
        assertThat(array.length()).isEqualTo(0);
    }

    @Test
    public void create_from_nio_buffer_readonly() {
        CharBuffer nioBuffer = CharBuffer.wrap("foobar", 2, 5);
        assertThat(nioBuffer.isReadOnly()).isTrue();

        CharArray array = new CharArray(nioBuffer);
        assertThat(new CharArray("oba")).isEqualTo(array);
        assertThat("oba".toCharArray()).isEqualTo(array.chars);
        assertThat(0).isEqualTo(array.start);
        assertThat(3).isEqualTo(array.end);
    }

    @Test
    public void create_from_nio_buffer_writable() {
        CharBuffer nioBuffer = CharBuffer.wrap("foobar".toCharArray(), 2, 3);
        assertThat(nioBuffer.isReadOnly()).isFalse();

        CharArray array = new CharArray(nioBuffer);
        assertThat(new CharArray("oba")).isEqualTo(array);
        assertThat("foobar".toCharArray()).isEqualTo(array.chars);
        assertThat(2).isEqualTo(array.start);
        assertThat(5).isEqualTo(array.end);
    }

    @Test
    public void equals_and_hashCode() {
        assertAlso(new CharArray("")).isEquivalentTo(new CharArray(""));
        assertAlso(new CharArray("")).isEquivalentTo(new CharArray("foo", 0, 0));
        assertAlso(new CharArray("")).isEquivalentTo(new CharArray("foo", 1, 1));
        assertAlso(new CharArray("")).isEquivalentTo(new CharArray("foo", 2, 2));
        assertAlso(new CharArray("")).isEquivalentTo(new CharArray("foo", 3, 3));
        assertAlso(new CharArray("foo")).isEquivalentTo(new CharArray("foo"));
        assertAlso(new CharArray("foo")).isEquivalentTo(new CharArray("foobar", 0, 3));
        assertAlso(new CharArray("foo")).isEquivalentTo(new CharArray("barfoo", 3, 6));
    }

    @Test
    public void create_invalid_pointers() {
        // noinspection ConstantConditions
        assertThrows(AssertionError.class, () -> new CharArray((char[]) null, 0, 0));
        assertThrows(AssertionError.class, () -> new CharArray("foo", -1, 2));
        assertThrows(AssertionError.class, () -> new CharArray("foo", 2, 1));
        assertThrows(AssertionError.class, () -> new CharArray("foo", 0, 4));
        assertThrows(AssertionError.class, () -> new CharArray("foo", 4, 4));
    }

    @Test
    public void char_at() {
        CharArray array = new CharArray("foobar");

        assertThat(array.charAt(0)).isEqualTo('f');
        assertThat(array.charAt(1)).isEqualTo('o');
        assertThat(array.charAt(2)).isEqualTo('o');
        assertThat(array.charAt(3)).isEqualTo('b');
        assertThat(array.charAt(4)).isEqualTo('a');
        assertThat(array.charAt(5)).isEqualTo('r');
        assertThrows(AssertionError.class, () -> array.charAt(-1));
        assertThrows(AssertionError.class, () -> array.charAt(6));
    }

    @Test
    public void at() {
        CharArray array = new CharArray("foobar");

        assertThat(array.at(0)).isEqualTo('f');
        assertThat(array.at(1)).isEqualTo('o');
        assertThat(array.at(2)).isEqualTo('o');
        assertThat(array.at(3)).isEqualTo('b');
        assertThat(array.at(4)).isEqualTo('a');
        assertThat(array.at(5)).isEqualTo('r');
        assertThat(array.at(6)).isEqualTo(-1);
        assertThat(array.at(-1)).isEqualTo('r');
        assertThat(array.at(-2)).isEqualTo('a');
        assertThat(array.at(-3)).isEqualTo('b');
        assertThat(array.at(-4)).isEqualTo('o');
        assertThat(array.at(-5)).isEqualTo('o');
        assertThat(array.at(-6)).isEqualTo('f');
        assertThat(array.at(-7)).isEqualTo(-1);
        assertThat(array.at(+100)).isEqualTo(-1);
        assertThat(array.at(-100)).isEqualTo(-1);
    }

    @Test
    public void indexOf() {
        CharArray array = new CharArray("foo-bar-baz");

        assertThat(array.indexOf('f')).isEqualTo(0);
        assertThat(array.indexOf('o')).isEqualTo(1);
        assertThat(array.indexOf('-')).isEqualTo(3);
        assertThat(array.indexOf('a')).isEqualTo(5);
        assertThat(array.indexOf('z')).isEqualTo(10);
        assertThat(array.indexOf('w')).isEqualTo(-1);

        assertThat(array.indexOf('f', 1)).isEqualTo(-1);
        assertThat(array.indexOf('f', 1, -2)).isEqualTo(-2);
        assertThat(array.indexOf('f', 1, array.length())).isEqualTo(11);
        assertThat(array.indexOf('o', 1)).isEqualTo(1);
        assertThat(array.indexOf('o', 1, -2)).isEqualTo(1);
        assertThat(array.indexOf('-', 3)).isEqualTo(3);
        assertThat(array.indexOf('-', 3, array.length())).isEqualTo(3);
        assertThat(array.indexOf('-', 4)).isEqualTo(7);
        assertThat(array.indexOf('-', 4, array.length())).isEqualTo(7);
        assertThat(array.indexOf('-', 4, -2)).isEqualTo(7);
        assertThat(array.indexOf('-', 7, array.length())).isEqualTo(7);
        assertThat(array.indexOf('-', 8, array.length())).isEqualTo(11);
    }

    @Test
    public void indexOf_subarray() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        assertThat(array.indexOf('f')).isEqualTo(-1);
        assertThat(array.indexOf('a')).isEqualTo(-1);
        assertThat(array.indexOf('b')).isEqualTo(2);
        assertThat(array.indexOf('o', 3)).isEqualTo(-1);
        assertThat(array.indexOf('o', 3, -2)).isEqualTo(-2);
    }

    @Test
    public void indexOfAny() {
        CharArray array = new CharArray("foo-bar-baz");

        assertThat(array.indexOfAny('f', 'o')).isEqualTo(0);
        assertThat(array.indexOfAny('o', 'o')).isEqualTo(1);
        assertThat(array.indexOfAny('a', '-')).isEqualTo(3);
        assertThat(array.indexOfAny('a', 'z')).isEqualTo(5);
        assertThat(array.indexOfAny('z', 'w')).isEqualTo(10);
        assertThat(array.indexOfAny('x', 'y')).isEqualTo(-1);

        assertThat(array.indexOfAny('f', 'g', 1)).isEqualTo(-1);
        assertThat(array.indexOfAny('f', 'g', 1, -2)).isEqualTo(-2);
        assertThat(array.indexOfAny('f', 'g', 1, array.length())).isEqualTo(11);
        assertThat(array.indexOfAny('o', 'a', 1)).isEqualTo(1);
        assertThat(array.indexOfAny('o', 'a', 1, -2)).isEqualTo(1);
        assertThat(array.indexOfAny('-', 'a', 3)).isEqualTo(3);
        assertThat(array.indexOfAny('-', 'a', 3, array.length())).isEqualTo(3);
        assertThat(array.indexOfAny('z', '-', 4)).isEqualTo(7);
        assertThat(array.indexOfAny('z', '-', 4, array.length())).isEqualTo(7);
        assertThat(array.indexOfAny('z', '-', 4, -2)).isEqualTo(7);
        assertThat(array.indexOfAny('z', '-', 7, array.length())).isEqualTo(7);
        assertThat(array.indexOfAny('a', '-', 8, array.length())).isEqualTo(9);
        assertThat(array.indexOfAny('z', '-', 8, array.length())).isEqualTo(10);
        assertThat(array.indexOfAny('z', '-', 11, array.length())).isEqualTo(11);
        assertThat(array.indexOfAny('o', '-', 8, array.length())).isEqualTo(11);
    }

    @Test
    public void indexOfAny_subarray() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        assertThat(array.indexOfAny('f', 'a')).isEqualTo(-1);
        assertThat(array.indexOfAny('f', 'b')).isEqualTo(2);
        assertThat(array.indexOfAny('o', 'o', 3)).isEqualTo(-1);
        assertThat(array.indexOfAny('o', 'o', 3, -2)).isEqualTo(-2);
    }

    @Test
    public void lastIndexOf() {
        CharArray array = new CharArray("foo-bar-baz");

        assertThat(array.lastIndexOf('f')).isEqualTo(0);
        assertThat(array.lastIndexOf('o')).isEqualTo(2);
        assertThat(array.lastIndexOf('-')).isEqualTo(7);
        assertThat(array.lastIndexOf('a')).isEqualTo(9);
        assertThat(array.lastIndexOf('z')).isEqualTo(10);
        assertThat(array.lastIndexOf('w')).isEqualTo(-1);

        assertThat(array.lastIndexOf('f', 10)).isEqualTo(0);
        assertThat(array.lastIndexOf('f', 10, -1)).isEqualTo(0);
        assertThat(array.lastIndexOf('a', 10)).isEqualTo(9);
        assertThat(array.lastIndexOf('a', 9)).isEqualTo(9);
        assertThat(array.lastIndexOf('a', 8)).isEqualTo(5);
        assertThat(array.lastIndexOf('a', 5)).isEqualTo(5);
        assertThat(array.lastIndexOf('a', 4)).isEqualTo(-1);
        assertThat(array.lastIndexOf('a', 4, -2)).isEqualTo(-2);
    }

    @Test
    public void lastIndexOf_subarray() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        assertThat(array.lastIndexOf('f')).isEqualTo(-1);
        assertThat(array.lastIndexOf('a')).isEqualTo(-1);
        assertThat(array.lastIndexOf('a', 2)).isEqualTo(-1);
        assertThat(array.lastIndexOf('a', 3)).isEqualTo(-1);
        assertThat(array.lastIndexOf('b')).isEqualTo(2);
        assertThat(array.lastIndexOf('b', 2)).isEqualTo(2);
        assertThat(array.lastIndexOf('b', 3)).isEqualTo(2);
        assertThat(array.lastIndexOf('o', 0)).isEqualTo(0);
        assertThat(array.lastIndexOf('o', 3)).isEqualTo(1);
        assertThat(array.lastIndexOf('o', 3, -2)).isEqualTo(1);
    }

    @Test
    public void lastIndexOfAny() {
        CharArray array = new CharArray("foo-bar-baz");

        assertThat(array.lastIndexOfAny('f', 'g')).isEqualTo(0);
        assertThat(array.lastIndexOfAny('f', 'o')).isEqualTo(2);
        assertThat(array.lastIndexOfAny('o', 'f')).isEqualTo(2);
        assertThat(array.lastIndexOfAny('o', 'x')).isEqualTo(2);
        assertThat(array.lastIndexOfAny('-', 'o')).isEqualTo(7);
        assertThat(array.lastIndexOfAny('-', 'a')).isEqualTo(9);
        assertThat(array.lastIndexOfAny('-', 'z')).isEqualTo(10);
        assertThat(array.lastIndexOfAny('x', 'y')).isEqualTo(-1);

        assertThat(array.lastIndexOfAny('f', 'g', 10)).isEqualTo(0);
        assertThat(array.lastIndexOfAny('f', 'g', 10, -1)).isEqualTo(0);
        assertThat(array.lastIndexOfAny('f', 'z', 10)).isEqualTo(10);
        assertThat(array.lastIndexOfAny('f', 'z', 10, -1)).isEqualTo(10);
        assertThat(array.lastIndexOfAny('a', 'z', 10)).isEqualTo(10);
        assertThat(array.lastIndexOfAny('a', 'r', 10)).isEqualTo(9);
        assertThat(array.lastIndexOfAny('a', 'z', 9)).isEqualTo(9);
        assertThat(array.lastIndexOfAny('a', 'z', 8)).isEqualTo(5);
        assertThat(array.lastIndexOfAny('a', 'r', 5)).isEqualTo(5);
        assertThat(array.lastIndexOfAny('a', 'r', 4)).isEqualTo(-1);
        assertThat(array.lastIndexOfAny('a', 'r', 4, -2)).isEqualTo(-2);
    }

    @Test
    public void lastIndexOfAny_subarray() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        assertThat(array.lastIndexOfAny('f', 'a')).isEqualTo(-1);
        assertThat(array.lastIndexOfAny('a', 'f')).isEqualTo(-1);
        assertThat(array.lastIndexOfAny('a', 'f', 2)).isEqualTo(-1);
        assertThat(array.lastIndexOfAny('a', 'f', 3)).isEqualTo(-1);
        assertThat(array.lastIndexOfAny('b', 'o')).isEqualTo(2);
        assertThat(array.lastIndexOfAny('b', 'o', 2)).isEqualTo(2);
        assertThat(array.lastIndexOfAny('b', 'o', 3)).isEqualTo(2);
        assertThat(array.lastIndexOfAny('o', 'f', 0)).isEqualTo(0);
        assertThat(array.lastIndexOfAny('o', 'f', 3)).isEqualTo(1);
        assertThat(array.lastIndexOfAny('o', 'f', 3, -2)).isEqualTo(1);
    }

    @Test
    public void contains() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        assertThat(array.contains('o')).isTrue();
        assertThat(array.contains('b')).isTrue();
        assertThat(array.contains('f')).isFalse();
        assertThat(array.contains('a')).isFalse();
        assertThat(array.contains('x')).isFalse();
    }

    @Test
    public void containsAny() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        assertThat(array.containsAny('o', 'b')).isTrue();
        assertThat(array.containsAny('o', 'x')).isTrue();
        assertThat(array.containsAny('b', 'x')).isTrue();
        assertThat(array.containsAny('b', 'a')).isTrue();
        assertThat(array.containsAny('a', 'x')).isFalse();
    }

    @Test
    public void split_by_char() {
        assertThat(new CharArray("").split('.')).containsExactly(new CharArray(""));
        assertThat(new CharArray(".").split('.')).containsExactly(new CharArray(""), new CharArray(""));
        assertThat(new CharArray("..").split('.')).containsExactly(new CharArray(""), new CharArray(""), new CharArray(""));
        assertThat(new CharArray("a.").split('.')).containsExactly(new CharArray("a"), new CharArray(""));
        assertThat(new CharArray("a.b").split('.')).containsExactly(new CharArray("a"), new CharArray("b"));
        assertThat(new CharArray("a..b").split('.')).containsExactly(new CharArray("a"), new CharArray(""), new CharArray("b"));
        assertThat(new CharArray("ab").split('.')).containsExactly(new CharArray("ab"));
        assertThat(new CharArray("ab.").split('.')).containsExactly(new CharArray("ab"), new CharArray(""));
        assertThat(new CharArray(".ab").split('.')).containsExactly(new CharArray(""), new CharArray("ab"));
        assertThat(new CharArray("ab.cd").split('.')).containsExactly(new CharArray("ab"), new CharArray("cd"));
        assertThat(new CharArray("a.b.c").split('.')).containsExactly(new CharArray("a"), new CharArray("b"), new CharArray("c"));
    }

    @Test
    public void trim_by_predicate() {
        assertThat(new CharArray("").trimStart(Character::isDigit)).isEqualTo(new CharArray(""));
        assertThat(new CharArray("").trimStart(Character::isDigit)).isEqualTo(new CharArray(""));
        assertThat(new CharArray("").trimStart(Character::isDigit)).isEqualTo(new CharArray(""));

        assertThat(new CharArray("foobar").trimStart(Character::isDigit)).isEqualTo(new CharArray("foobar"));
        assertThat(new CharArray("foobar").trimEnd(Character::isDigit)).isEqualTo(new CharArray("foobar"));
        assertThat(new CharArray("foobar").trim(Character::isDigit)).isEqualTo(new CharArray("foobar"));

        assertThat(new CharArray("123foobar456").trimStart(Character::isDigit)).isEqualTo(new CharArray("foobar456"));
        assertThat(new CharArray("123foobar456").trimEnd(Character::isDigit)).isEqualTo(new CharArray("123foobar"));
        assertThat(new CharArray("123foobar456").trim(Character::isDigit)).isEqualTo(new CharArray("foobar"));

        assertThat(new CharArray("123").trimStart(Character::isDigit)).isEqualTo(new CharArray(""));
        assertThat(new CharArray("123").trimStart(Character::isDigit)).isEqualTo(new CharArray(""));
        assertThat(new CharArray("123").trimStart(Character::isDigit)).isEqualTo(new CharArray(""));
    }

    @Test
    public void trim_char() {
        assertThat(new CharArray("").trim('a')).isEqualTo(new CharArray(""));
        assertThat(new CharArray("bbb").trim('a')).isEqualTo(new CharArray("bbb"));
        assertThat(new CharArray("abba").trim('a')).isEqualTo(new CharArray("bb"));
        assertThat(new CharArray("baba").trim('a')).isEqualTo(new CharArray("bab"));
        assertThat(new CharArray("aba").trim('a')).isEqualTo(new CharArray("b"));
        assertThat(new CharArray("a-a-a").trim('a')).isEqualTo(new CharArray("-a-"));
        assertThat(new CharArray("aaa").trim('a')).isEqualTo(new CharArray(""));
    }

    @Test
    public void trim_spaces() {
        assertThat(new CharArray("").trim()).isEqualTo(new CharArray(""));
        assertThat(new CharArray(" ").trim()).isEqualTo(new CharArray(""));
        assertThat(new CharArray("    ").trim()).isEqualTo(new CharArray(""));
        assertThat(new CharArray("  \t\n\r").trim()).isEqualTo(new CharArray(""));
        assertThat(new CharArray("\nfoo bar  \t\t").trim()).isEqualTo(new CharArray("foo bar"));
    }

    @Test
    public void commonPrefix() {
        assertThat(new CharArray("foo").commonPrefix("bar")).isEqualTo(0);
        assertThat(new CharArray("bar").commonPrefix("baz")).isEqualTo(2);
        assertThat(new CharArray("foo").commonPrefix("foo")).isEqualTo(3);

        assertThat(new CharArray("foo").commonPrefix(new CharArray("bar"))).isEqualTo(0);
        assertThat(new CharArray("bar").commonPrefix(new CharArray("baz"))).isEqualTo(2);

        assertThat(new CharArray("foo").commonPrefix(new CharArray("foobar", 3, 6))).isEqualTo(0);
        assertThat(new CharArray("bar").commonPrefix(new CharArray("barbaz", 3, 6))).isEqualTo(2);
        assertThat(new CharArray("foobar", 3, 6).commonPrefix(new CharArray("foo"))).isEqualTo(0);
        assertThat(new CharArray("barbaz", 3, 6).commonPrefix(new CharArray("bar"))).isEqualTo(2);
    }

    @Test
    public void commonPrefix_empty() {
        assertThat(new CharArray("").commonPrefix(new CharArray(""))).isEqualTo(0);
        assertThat(new CharArray("foo").commonPrefix(new CharArray(""))).isEqualTo(0);
        assertThat(new CharArray("").commonPrefix(new CharArray("foo"))).isEqualTo(0);

        assertThat(new CharArray("foo", 1, 2).commonPrefix(new CharArray("foo", 3, 3))).isEqualTo(0);
        assertThat(new CharArray("xxx", 1, 1).commonPrefix(new CharArray("xxx", 2, 2))).isEqualTo(0);
    }

    @Test
    public void commonPrefix_same_prefix() {
        assertThat(new CharArray("foo").commonPrefix(new CharArray("foo"))).isEqualTo(3);
        assertThat(new CharArray("foo").commonPrefix(new CharArray("foobar"))).isEqualTo(3);
        assertThat(new CharArray("foobar").commonPrefix(new CharArray("foo"))).isEqualTo(3);

        assertThat(new CharArray("foo").commonPrefix(new CharArray("barfoo", 3, 6))).isEqualTo(3);
        assertThat(new CharArray("barfoo", 3, 6).commonPrefix(new CharArray("foo"))).isEqualTo(3);
    }

    @Test
    public void commonSuffix() {
        assertThat(new CharArray("foo").commonSuffix("bar")).isEqualTo(0);
        assertThat(new CharArray("foo").commonSuffix("boo")).isEqualTo(2);
        assertThat(new CharArray("foo").commonSuffix("foo")).isEqualTo(3);

        assertThat(new CharArray("foo").commonSuffix(new CharArray("bar"))).isEqualTo(0);
        assertThat(new CharArray("foo").commonSuffix(new CharArray("boo"))).isEqualTo(2);

        assertThat(new CharArray("foo").commonSuffix(new CharArray("foobar", 3, 6))).isEqualTo(0);
        assertThat(new CharArray("foo").commonSuffix(new CharArray("fooboo", 3, 6))).isEqualTo(2);
        assertThat(new CharArray("foobar", 3, 6).commonSuffix(new CharArray("foo"))).isEqualTo(0);
        assertThat(new CharArray("fooboo", 3, 6).commonSuffix(new CharArray("foo"))).isEqualTo(2);
    }

    @Test
    public void commonSuffix_empty() {
        assertThat(new CharArray("").commonSuffix(new CharArray(""))).isEqualTo(0);
        assertThat(new CharArray("foo").commonSuffix(new CharArray(""))).isEqualTo(0);
        assertThat(new CharArray("").commonSuffix(new CharArray("foo"))).isEqualTo(0);

        assertThat(new CharArray("foo", 1, 2).commonSuffix(new CharArray("foo", 3, 3))).isEqualTo(0);
        assertThat(new CharArray("xxx", 1, 1).commonSuffix(new CharArray("xxx", 2, 2))).isEqualTo(0);
    }

    @Test
    public void commonSuffix_same_suffix() {
        assertThat(new CharArray("foo").commonSuffix(new CharArray("foo"))).isEqualTo(3);
        assertThat(new CharArray("foo").commonSuffix(new CharArray("barfoo"))).isEqualTo(3);
        assertThat(new CharArray("barfoo").commonSuffix(new CharArray("foo"))).isEqualTo(3);

        assertThat(new CharArray("foo").commonSuffix(new CharArray("barfoo", 3, 6))).isEqualTo(3);
        assertThat(new CharArray("barfoo", 3, 6).commonSuffix(new CharArray("foo"))).isEqualTo(3);
    }

    @Test
    public void startsWith() {
        CharArray array = new CharArray("foo");
        assertThat(array.startsWith(new CharArray(""))).isTrue();
        assertThat(array.startsWith(new CharArray("f"))).isTrue();
        assertThat(array.startsWith(new CharArray("fo"))).isTrue();
        assertThat(array.startsWith(new CharArray("foo"))).isTrue();

        assertThat(array.startsWith(new CharArray("x"))).isFalse();
        assertThat(array.startsWith(new CharArray("bar"))).isFalse();
        assertThat(array.startsWith(new CharArray("foe"))).isFalse();
        assertThat(array.startsWith(new CharArray("foo!"))).isFalse();
        assertThat(array.startsWith(new CharArray("foobar"))).isFalse();
    }

    @Test
    public void startsWith_string() {
        CharArray array = new CharArray("foo");
        assertThat(array.startsWith("")).isTrue();
        assertThat(array.startsWith("f")).isTrue();
        assertThat(array.startsWith("fo")).isTrue();
        assertThat(array.startsWith("foo")).isTrue();

        assertThat(array.startsWith("x")).isFalse();
        assertThat(array.startsWith("bar")).isFalse();
        assertThat(array.startsWith("foe")).isFalse();
        assertThat(array.startsWith("foo!")).isFalse();
        assertThat(array.startsWith("foobar")).isFalse();
    }

    @Test
    public void startsWith_char() {
        CharArray array = new CharArray("foo");
        assertThat(array.startsWith('f')).isTrue();
        assertThat(array.startsWith('o')).isFalse();
        assertThat(array.startsWith('a')).isFalse();
        assertThat(array.startsWith('x')).isFalse();

        assertThat(new CharArray("").startsWith(' ')).isFalse();
    }

    @Test
    public void endsWith() {
        CharArray array = new CharArray("foo");
        assertThat(array.endsWith(new CharArray(""))).isTrue();
        assertThat(array.endsWith(new CharArray("o"))).isTrue();
        assertThat(array.endsWith(new CharArray("oo"))).isTrue();
        assertThat(array.endsWith(new CharArray("foo"))).isTrue();

        assertThat(array.endsWith(new CharArray("x"))).isFalse();
        assertThat(array.endsWith(new CharArray("bar"))).isFalse();
        assertThat(array.endsWith(new CharArray("boo"))).isFalse();
        assertThat(array.endsWith(new CharArray("!foo"))).isFalse();
        assertThat(array.endsWith(new CharArray("barfoo"))).isFalse();
    }

    @Test
    public void endsWith_string() {
        CharArray array = new CharArray("foo");
        assertThat(array.endsWith("")).isTrue();
        assertThat(array.endsWith("o")).isTrue();
        assertThat(array.endsWith("oo")).isTrue();
        assertThat(array.endsWith("foo")).isTrue();

        assertThat(array.endsWith("x")).isFalse();
        assertThat(array.endsWith("bar")).isFalse();
        assertThat(array.endsWith("boo")).isFalse();
        assertThat(array.endsWith("!foo")).isFalse();
        assertThat(array.endsWith("barfoo")).isFalse();
    }

    @Test
    public void endsWith_char() {
        CharArray array = new CharArray("foo");
        assertThat(array.endsWith('o')).isTrue();
        assertThat(array.endsWith('f')).isFalse();
        assertThat(array.endsWith('a')).isFalse();
        assertThat(array.endsWith('x')).isFalse();

        assertThat(new CharArray("").endsWith(' ')).isFalse();
    }

    @Test
    public void substring_valid() {
        assertThat(new CharArray("foobar").substring(0, 3)).isEqualTo(new CharArray("foo"));
        assertThat(new CharArray("foobar").substring(3, 6)).isEqualTo(new CharArray("bar"));
        assertThat(new CharArray("foobar").substring(0, 1)).isEqualTo(new CharArray("f"));
        assertThat(new CharArray("foobar").substring(1, 1)).isEqualTo(new CharArray(""));
        assertThat(new CharArray("foobar").substring(5, 6)).isEqualTo(new CharArray("r"));
        assertThat(new CharArray("foobar").substring(6, 6)).isEqualTo(new CharArray(""));

        assertThat(new CharArray("foobar").substring(0, -1)).isEqualTo(new CharArray("fooba"));
        assertThat(new CharArray("foobar").substring(0, -2)).isEqualTo(new CharArray("foob"));
        assertThat(new CharArray("foobar").substring(0, -3)).isEqualTo(new CharArray("foo"));
        assertThat(new CharArray("foobar").substring(-3, 6)).isEqualTo(new CharArray("bar"));
        assertThat(new CharArray("foobar").substring(-3, -1)).isEqualTo(new CharArray("ba"));
        assertThat(new CharArray("foobar").substring(-3, -2)).isEqualTo(new CharArray("b"));
        assertThat(new CharArray("foobar").substring(-3, -3)).isEqualTo(new CharArray(""));

        assertThat(new CharArray("foobar").substring(0, 1)).isEqualTo(new CharArray("f"));
        assertThat(new CharArray("foobar").substring(0, 0)).isEqualTo(new CharArray(""));
        assertThat(new CharArray("foobar").substring(1, 1)).isEqualTo(new CharArray(""));
        assertThat(new CharArray("foobar").substring(0, -6)).isEqualTo(new CharArray(""));
        assertThat(new CharArray("foobar").substring(1, -5)).isEqualTo(new CharArray(""));
    }

    @Test
    public void substring_invalid() {
        assertThrows(AssertionError.class, () -> new CharArray("foobar").substring(0, 7));
        assertThrows(AssertionError.class, () -> new CharArray("foobar").substring(7, 7));
        assertThrows(AssertionError.class, () -> new CharArray("foobar").substring(7, 8));
        assertThrows(AssertionError.class, () -> new CharArray("foobar").substring(0, -10));
        assertThrows(AssertionError.class, () -> new CharArray("foobar").substring(-10, 6));
        assertThrows(AssertionError.class, () -> new CharArray("foobar").substring(1, 0));
    }

    @Test
    public void join_same_buffer() {
        CharArray array = new CharArray("foobar");
        CharArray foo = array.substringUntil(3);
        CharArray bar = array.substringFrom(3);
        CharArray join = CharArray.join(foo, bar);

        assertThat(array.chars).isSameInstanceAs(foo.chars);
        assertThat(array.chars).isSameInstanceAs(bar.chars);
        assertThat(array.chars).isSameInstanceAs(join.chars);
        assertThat(array).isEqualTo(join);
    }

    @Test
    public void join_not_same_buffer() {
        CharArray array = new CharArray("foobar");
        CharArray foo = new CharArray("foo");
        CharArray bar = new CharArray("bar");
        CharArray join = CharArray.join(foo, bar);

        assertThat(array.chars).isNotSameInstanceAs(foo.chars);
        assertThat(array.chars).isNotSameInstanceAs(bar.chars);
        assertThat(array.chars).isNotSameInstanceAs(join.chars);
        assertThat(array).isEqualTo(join);
    }

    @Test
    public void cutPrefix_array() {
        CharArray foobar = new CharArray("foobar");
        CharArray foo = new CharArray("foo");
        CharArray bar = new CharArray("bar");
        CharArray empty = new CharArray("");

        assertThat(foobar.cutPrefix(empty)).isEqualTo(foobar);
        assertThat(foobar.cutPrefix(foo)).isEqualTo(bar);
        assertThat(foobar.cutPrefix(foo)).isEqualTo(bar);
        assertThat(foobar.cutPrefix(foobar)).isEqualTo(empty);

        assertThat(foobar.substringFrom(3)).isEqualTo(bar);
        assertThat(foobar.substringFrom(3).cutPrefix(foo)).isEqualTo(bar);
        assertThat(foobar.substringFrom(3).cutPrefix(bar)).isEqualTo(empty);
        assertThat(foobar.substringFrom(3).cutPrefix(bar.substringUntil(0))).isEqualTo(bar);
        assertThat(foobar.substringFrom(3).cutPrefix(bar.substringUntil(1))).isEqualTo(new CharArray("ar"));  // cut b
        assertThat(foobar.substringFrom(3).cutPrefix(bar.substringUntil(2))).isEqualTo(new CharArray("r"));   // cut ba
    }

    @Test
    public void cutPrefix_str() {
        assertThat(new CharArray("foobar").cutPrefix("")).isEqualTo(new CharArray("foobar"));
        assertThat(new CharArray("foobar").cutPrefix("foo")).isEqualTo(new CharArray("bar"));
        assertThat(new CharArray("foobar").cutPrefix("bar")).isEqualTo(new CharArray("foobar"));
        assertThat(new CharArray("foobar").cutPrefix("fooba")).isEqualTo(new CharArray("r"));
        assertThat(new CharArray("foobar").cutPrefix("foobar")).isEqualTo(new CharArray(""));
        assertThat(new CharArray("foobar").cutPrefix("foobarbaz")).isEqualTo(new CharArray("foobar"));
        assertThat(new CharArray("foobar").cutPrefix('f')).isEqualTo(new CharArray("oobar"));
        assertThat(new CharArray("foobar").cutPrefix('o')).isEqualTo(new CharArray("foobar"));
    }

    @Test
    public void cutSuffix_array() {
        CharArray foobar = new CharArray("foobar");
        CharArray foo = new CharArray("foo");
        CharArray bar = new CharArray("bar");
        CharArray empty = new CharArray("");

        assertThat(foobar.cutSuffix(empty)).isEqualTo(foobar);
        assertThat(foobar.cutSuffix(bar)).isEqualTo(foo);
        assertThat(foobar.cutSuffix(foo)).isEqualTo(foobar);
        assertThat(foobar.cutSuffix(foobar)).isEqualTo(empty);

        assertThat(foobar.substringFrom(3)).isEqualTo(bar);
        assertThat(foobar.substringFrom(3).cutSuffix(foo)).isEqualTo(bar);
        assertThat(foobar.substringFrom(3).cutSuffix(bar)).isEqualTo(empty);
        assertThat(foobar.substringFrom(3).cutSuffix(bar.substringFrom(1))).isEqualTo(new CharArray("b"));   // cut ar
        assertThat(foobar.substringFrom(3).cutSuffix(bar.substringFrom(2))).isEqualTo(new CharArray("ba"));  // cut r
        assertThat(foobar.substringFrom(3).cutSuffix(bar.substringFrom(3))).isEqualTo(bar);
    }

    @Test
    public void cutSuffix_str() {
        assertThat(new CharArray("foobar").cutSuffix("")).isEqualTo(new CharArray("foobar"));
        assertThat(new CharArray("foobar").cutSuffix("bar")).isEqualTo(new CharArray("foo"));
        assertThat(new CharArray("foobar").cutSuffix("foo")).isEqualTo(new CharArray("foobar"));
        assertThat(new CharArray("foobar").cutSuffix("oobar")).isEqualTo(new CharArray("f"));
        assertThat(new CharArray("foobar").cutSuffix("foobar")).isEqualTo(new CharArray(""));
        assertThat(new CharArray("foobar").cutSuffix("foofoobar")).isEqualTo(new CharArray("foobar"));
        assertThat(new CharArray("foobar").cutSuffix('r')).isEqualTo(new CharArray("fooba"));
        assertThat(new CharArray("foobar").cutSuffix('a')).isEqualTo(new CharArray("foobar"));
    }

    @Test
    public void chars() {
        int[] array = new CharArray("foobar").chars().toArray();
        assertThat(array).isEqualTo(new int[] { 102, 111, 111, 98, 97, 114 });
    }

    @Test
    public void chars_empty() {
        int[] array = new CharArray("").chars().toArray();
        assertThat(array).isEqualTo(new int[0]);
    }

    @Test
    public void chars_of_subbuffer() {
        int[] array = new CharArray("foobar", 1, 3).chars().toArray();
        assertThat(array).isEqualTo(new int[] { 111, 111 });
    }

    @Test
    public void codepoints() {
        int[] array = new CharArray("foobar").codePoints().toArray();
        assertThat(array).isEqualTo(new int[] { 102, 111, 111, 98, 97, 114 });
    }

    @Test
    public void codepoints_empty() {
        int[] array = new CharArray("").codePoints().toArray();
        assertThat(array).isEqualTo(new int[0]);
    }

    @Test
    public void codepoints_of_subbuffer() {
        int[] array = new CharArray("foobar", 1, 3).codePoints().toArray();
        assertThat(array).isEqualTo(new int[] { 111, 111 });
    }

    @Test
    public void compareTo_array_simple() {
        assertThat(new CharArray("foo").compareTo(new CharArray("bar"))).isAtLeast(1);
        assertThat(new CharArray("foo").compareTo(new CharArray("foo"))).isEqualTo(0);
        assertThat(new CharArray("bar").compareTo(new CharArray("foo"))).isAtMost(-1);
        assertThat(new CharArray("foobar").compareTo(new CharArray("foo"))).isAtLeast(1);
        assertThat(new CharArray("foo").compareTo(new CharArray("foobar"))).isAtMost(-1);
    }

    @Test
    public void compareTo_array_empty() {
        assertThat(new CharArray("").compareTo(new CharArray(""))).isEqualTo(0);
        assertThat(new CharArray("foo").compareTo(new CharArray(""))).isAtLeast(1);
        assertThat(new CharArray("").compareTo(new CharArray("foo"))).isAtMost(-1);
    }

    @Test
    public void compareTo_string_simple() {
        assertThat(new CharArray("foo").compareTo("bar")).isAtLeast(1);
        assertThat(new CharArray("foo").compareTo("foo")).isEqualTo(0);
        assertThat(new CharArray("bar").compareTo("foo")).isAtMost(-1);
        assertThat(new CharArray("foobar").compareTo("foo")).isAtLeast(1);
        assertThat(new CharArray("foo").compareTo("foobar")).isAtMost(-1);
    }

    @Test
    public void compareTo_string_empty() {
        assertThat(new CharArray("").compareTo("")).isEqualTo(0);
        assertThat(new CharArray("foo").compareTo("")).isAtLeast(1);
        assertThat(new CharArray("").compareTo("foo")).isAtMost(-1);
    }

    @Test
    public void contentEquals_char() {
        assertThat(new CharArray("").contentEquals('a')).isFalse();
        assertThat(new CharArray("a").contentEquals('a')).isTrue();
        assertThat(new CharArray("ab").contentEquals('a')).isFalse();
        assertThat(new CharArray("b").contentEquals('a')).isFalse();
    }

    @Test
    public void contentEquals_string() {
        assertThat(new CharArray("").contentEquals("")).isTrue();
        assertThat(new CharArray("").contentEquals("foo")).isFalse();

        assertThat(new CharArray("a").contentEquals("")).isFalse();
        assertThat(new CharArray("a").contentEquals("a")).isTrue();
        assertThat(new CharArray("a").contentEquals("A")).isFalse();
        assertThat(new CharArray("a").contentEquals("foo")).isFalse();

        assertThat(new CharArray("foo").contentEquals("")).isFalse();
        assertThat(new CharArray("foo").contentEquals("a")).isFalse();
        assertThat(new CharArray("foo").contentEquals("foo")).isTrue();
        assertThat(new CharArray("foo").contentEquals("Foo")).isFalse();
    }

    @Test
    public void equalsIgnoreCase() {
        assertThat(new CharArray("foo").equalsIgnoreCase("foo")).isTrue();
        assertThat(new CharArray("foo").equalsIgnoreCase("FOO")).isTrue();
        assertThat(new CharArray("foo").equalsIgnoreCase("FoO")).isTrue();

        assertThat(new CharArray("foo").equalsIgnoreCase("foo.")).isFalse();
        assertThat(new CharArray("foo").equalsIgnoreCase("fooo")).isFalse();
        assertThat(new CharArray("foo").equalsIgnoreCase("bar")).isFalse();
        assertThat(new CharArray("foo").equalsIgnoreCase("oo")).isFalse();
        assertThat(new CharArray("foo").equalsIgnoreCase("fof")).isFalse();
        assertThat(new CharArray("foo").equalsIgnoreCase("")).isFalse();
    }
}
