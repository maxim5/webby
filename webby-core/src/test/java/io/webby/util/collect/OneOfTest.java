package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OneOfTest {
    @Test
    public void oneOf_simple() {
        assertOneOf(OneOf.ofFirst(1), 1, null);
        assertOneOf(OneOf.ofSecond(2), null, 2);
        assertOneOf(OneOf.of(1, null), 1, null);
        assertOneOf(OneOf.of(null, 2), null, 2);
    }

    @Test
    public void oneOf_invalid() {
        assertThrows(AssertionError.class, () -> OneOf.ofFirst(null));
        assertThrows(AssertionError.class, () -> OneOf.ofSecond(null));
        assertThrows(AssertionError.class, () -> OneOf.of(1, 2));
        assertThrows(AssertionError.class, () -> OneOf.of(null, null));
    }

    @Test
    public void oneOf_map() {
        assertOneOf(OneOf.ofFirst(1).mapFirst(x -> -x), -1, null);
        assertOneOf(OneOf.ofFirst(1).map(x -> -x, String::valueOf), -1, null);
        assertOneOf(OneOf.ofSecond(2).mapSecond(x -> -x), null, -2);
        assertOneOf(OneOf.ofSecond(2).map(String::valueOf, x -> -x), null, -2);
    }

    @Test
    public void oneOf_map_invalid() {
        assertThrows(AssertionError.class, () -> OneOf.of(1, null).mapSecond(String::valueOf));
        assertThrows(AssertionError.class, () -> OneOf.of(null, 2).mapFirst(String::valueOf));
    }

    @Test
    public void oneOf_mapToObj() {
        assertEquals(OneOf.ofFirst(1).mapToObj(String::valueOf, String::valueOf), "1");
        assertEquals(OneOf.ofSecond(2).mapToObj(String::valueOf, String::valueOf), "2");
    }

    @Test
    public void oneOf_mapToInt() {
        assertEquals(OneOf.ofFirst(1).mapToInt(Object::hashCode, Object::hashCode), 1);
        assertEquals(OneOf.ofSecond(2).mapToInt(Object::hashCode, Object::hashCode), 2);
    }

    private static <U, V> void assertOneOf(@NotNull OneOf<U, V> oneOf, @Nullable Object first, @Nullable Object second) {
        assertEquals(oneOf.hasFirst(), first != null);
        assertEquals(oneOf.hasSecond(), second != null);
        assertEquals(oneOf.first(), first);
        assertEquals(oneOf.second(), second);
        assertEquals(oneOf.getCase(), first != null ? OneOf.Which.FIRST : OneOf.Which.SECOND);
    }
}
