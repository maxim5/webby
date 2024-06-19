package io.spbx.util.base;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class EasyStringsTest {
    @Test
    public void removePrefix_simple() {
        assertThat(EasyStrings.removePrefix("", "")).isEqualTo("");
        assertThat(EasyStrings.removePrefix("", "foo")).isEqualTo("");

        assertThat(EasyStrings.removePrefix("foobar", "foo")).isEqualTo("bar");
        assertThat(EasyStrings.removePrefix("foobar", "")).isEqualTo("foobar");

        assertThat(EasyStrings.removePrefix("foobar", "fox")).isEqualTo("foobar");
        assertThat(EasyStrings.removePrefix("foobar", "Foo")).isEqualTo("foobar");
    }

    @Test
    public void removeSuffix_simple() {
        assertThat(EasyStrings.removeSuffix("", "")).isEqualTo("");
        assertThat(EasyStrings.removeSuffix("", "foo")).isEqualTo("");

        assertThat(EasyStrings.removeSuffix("foobar", "bar")).isEqualTo("foo");
        assertThat(EasyStrings.removeSuffix("foobar", "")).isEqualTo("foobar");

        assertThat(EasyStrings.removeSuffix("foobar", "baz")).isEqualTo("foobar");
        assertThat(EasyStrings.removeSuffix("foobar", "Bar")).isEqualTo("foobar");
    }
    
    @Test
    public void firstNotEmpty_simple() {
        assertThat(EasyStrings.firstNotEmpty("foo", "bar")).isEqualTo("foo");
        assertThat(EasyStrings.firstNotEmpty("foo", "")).isEqualTo("foo");
        assertThat(EasyStrings.firstNotEmpty("f", "")).isEqualTo("f");
        assertThat(EasyStrings.firstNotEmpty("", "bar")).isEqualTo("bar");
        assertThat(EasyStrings.firstNotEmpty("", "b")).isEqualTo("b");
    }
}
