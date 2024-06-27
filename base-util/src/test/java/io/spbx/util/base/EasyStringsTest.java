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
    public void ensurePrefix_simple() {
        assertThat(EasyStrings.ensurePrefix("", "")).isEqualTo("");
        assertThat(EasyStrings.ensurePrefix("", "foo")).isEqualTo("foo");

        assertThat(EasyStrings.ensurePrefix("foobar", "foo")).isEqualTo("foobar");
        assertThat(EasyStrings.ensurePrefix("foobar", "")).isEqualTo("foobar");

        assertThat(EasyStrings.ensurePrefix("foobar", "fox")).isEqualTo("foxfoobar");
        assertThat(EasyStrings.ensurePrefix("foobar", "Foo")).isEqualTo("Foofoobar");
    }
    
    @Test
    public void ensureSuffix_simple() {
        assertThat(EasyStrings.ensureSuffix("", "")).isEqualTo("");
        assertThat(EasyStrings.ensureSuffix("", "foo")).isEqualTo("foo");

        assertThat(EasyStrings.ensureSuffix("foobar", "foo")).isEqualTo("foobarfoo");
        assertThat(EasyStrings.ensureSuffix("foobar", "bar")).isEqualTo("foobar");
        assertThat(EasyStrings.ensureSuffix("foobar", "")).isEqualTo("foobar");

        assertThat(EasyStrings.ensureSuffix("foobar", "fox")).isEqualTo("foobarfox");
        assertThat(EasyStrings.ensureSuffix("foobar", "Foo")).isEqualTo("foobarFoo");
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
