package io.spbx.util.base;

import io.spbx.util.base.CharArray;
import io.spbx.util.base.MutableCharArray;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class MutableCharArrayTest {
    @Test
    public void create_empty_string() {
        MutableCharArray array = new MutableCharArray("");
        assertThat(array.start()).isEqualTo(0);
        assertThat(array.end()).isEqualTo(0);
        assertThat(array.length()).isEqualTo(0);
    }

    @Test
    public void create_empty_same_pointers() {
        MutableCharArray array = new MutableCharArray("foo", 3, 3);
        assertThat(array.start()).isEqualTo(3);
        assertThat(array.end()).isEqualTo(3);
        assertThat(array.length()).isEqualTo(0);
    }

    @Test
    public void immutable_to_others() {
        CharArray array = new CharArray("foo");

        assertThat(array.mutable()).isEqualTo(array);
        assertThat(array.mutableCopy()).isEqualTo(array);

        assertThat(array.immutable()).isSameInstanceAs(array);
        assertThat(array.immutableCopy()).isEqualTo(array);
        assertThat(array.immutable() instanceof MutableCharArray).isFalse();
        assertThat(array.immutableCopy() instanceof MutableCharArray).isFalse();
    }

    @Test
    public void mutable_to_others() {
        MutableCharArray array = new MutableCharArray("foo");

        assertThat(array.mutable()).isSameInstanceAs(array);
        assertThat(array.mutableCopy()).isEqualTo(array);
        assertThat(array.mutableCopy()).isNotSameInstanceAs(array);

        assertThat(array.immutable()).isEqualTo(array);
        assertThat(array.immutableCopy()).isEqualTo(array);
        assertThat(array.immutable() instanceof MutableCharArray).isFalse();
        assertThat(array.immutableCopy() instanceof MutableCharArray).isFalse();
    }

    @Test
    public void substring() {
        MutableCharArray array = new MutableCharArray("foobar");

        assertThat(array.substring(0, 3) instanceof MutableCharArray).isFalse();
        assertThat(array.substring(3, 6) instanceof MutableCharArray).isFalse();

        assertThat(array.mutableSubstring(0, 3)).isEqualTo(array.substring(0, 3));
        assertThat(array.mutableSubstring(1, 4)).isEqualTo(array.substring(1, 4));
        assertThat(array.mutableSubstring(3, 6)).isEqualTo(array.substring(3, 6));
    }

    @Test
    public void offset_start_and_end() {
        MutableCharArray array = new MutableCharArray("foobar");

        array.offsetStart(1);
        assertThat(array.toString()).isEqualTo("oobar");

        array.offsetEnd(1);
        assertThat(array.toString()).isEqualTo("ooba");

        array.offsetStart(2);
        assertThat(array.toString()).isEqualTo("ba");

        array.offsetEnd(2);
        assertThat(array.toString()).isEqualTo("");
    }

    @Test
    public void offset_prefix() {
        MutableCharArray array = new MutableCharArray("foobar");

        array.offsetPrefix(new CharArray("food"));
        assertThat(array.toString()).isEqualTo("foobar");

        array.offsetPrefix(new CharArray("foo"));
        assertThat(array.toString()).isEqualTo("bar");

        array.offsetPrefix(new CharArray("a"));
        assertThat(array.toString()).isEqualTo("bar");

        array.offsetPrefix(new CharArray("b"));
        assertThat(array.toString()).isEqualTo("ar");

        array.offsetPrefix('a');
        assertThat(array.toString()).isEqualTo("r");

        array.offsetPrefix('r');
        assertThat(array.toString()).isEqualTo("");
    }

    @Test
    public void offset_suffix() {
        MutableCharArray array = new MutableCharArray("foobar");

        array.offsetSuffix(new CharArray("var"));
        assertThat(array.toString()).isEqualTo("foobar");

        array.offsetSuffix(new CharArray("bar"));
        assertThat(array.toString()).isEqualTo("foo");

        array.offsetSuffix(new CharArray("a"));
        assertThat(array.toString()).isEqualTo("foo");

        array.offsetSuffix(new CharArray("o"));
        assertThat(array.toString()).isEqualTo("fo");

        array.offsetSuffix('o');
        assertThat(array.toString()).isEqualTo("f");

        array.offsetSuffix('f');
        assertThat(array.toString()).isEqualTo("");
    }

    @Test
    public void reset() {
        MutableCharArray array = new MutableCharArray("foobar", 3, 5);

        array.resetStart();
        assertThat(array.toString()).isEqualTo("fooba");

        array.resetEnd();
        assertThat(array.toString()).isEqualTo("foobar");

        array.offsetStart(2);
        array.offsetEnd(2);
        assertThat(array.toString()).isEqualTo("ob");

        array.reset();
        assertThat(array.toString()).isEqualTo("foobar");
    }

    @Test
    public void join_same_buffer() {
        MutableCharArray foobar = new MutableCharArray("foobar");
        CharArray foo = foobar.substring(0, 3);
        CharArray bar = foobar.substring(3, 6);

        assertThat(MutableCharArray.join(foo, bar)).isEqualTo(foobar);
        assertThat(MutableCharArray.join(foo, bar).chars).isSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo.immutableCopy(), bar).chars).isSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo, bar.immutableCopy()).chars).isSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo.immutableCopy(), bar.immutableCopy()).chars).isSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo.mutable(), bar).chars).isSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo, bar.mutable()).chars).isSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo.mutable(), bar.mutable()).chars).isSameInstanceAs(foobar.chars);
    }

    @Test
    public void join_new_buffer() {
        MutableCharArray foobar = new MutableCharArray("foobar");
        CharArray foo = new CharArray("foo");
        CharArray bar = new CharArray("bar");

        assertThat(MutableCharArray.join(foo, bar)).isEqualTo(foobar);
        assertThat(MutableCharArray.join(foo, bar).chars).isNotSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo.immutableCopy(), bar).chars).isNotSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo, bar.immutableCopy()).chars).isNotSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo.immutableCopy(), bar.immutableCopy()).chars).isNotSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo.mutable(), bar).chars).isNotSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo, bar.mutable()).chars).isNotSameInstanceAs(foobar.chars);
        assertThat(MutableCharArray.join(foo.mutable(), bar.mutable()).chars).isNotSameInstanceAs(foobar.chars);
    }
}
