package io.webby.util.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EasyStringsTest {
    @Test
    public void removePrefix_simple() {
        assertEquals(EasyStrings.removePrefix("", ""), "");
        assertEquals(EasyStrings.removePrefix("", "foo"), "");

        assertEquals(EasyStrings.removePrefix("foobar", "foo"), "bar");
        assertEquals(EasyStrings.removePrefix("foobar", ""), "foobar");

        assertEquals(EasyStrings.removePrefix("foobar", "fox"), "foobar");
        assertEquals(EasyStrings.removePrefix("foobar", "Foo"), "foobar");
    }

    @Test
    public void removeSuffix_simple() {
        assertEquals(EasyStrings.removeSuffix("", ""), "");
        assertEquals(EasyStrings.removeSuffix("", "foo"), "");

        assertEquals(EasyStrings.removeSuffix("foobar", "bar"), "foo");
        assertEquals(EasyStrings.removeSuffix("foobar", ""), "foobar");

        assertEquals(EasyStrings.removeSuffix("foobar", "baz"), "foobar");
        assertEquals(EasyStrings.removeSuffix("foobar", "Bar"), "foobar");
    }
}
