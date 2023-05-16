package io.webby.util.collect;

import com.google.common.base.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;

import static io.webby.testing.TestingBasics.array;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PairTest {
    @Test
    public void pair_simple_constructors() {
        assertPair(Pair.of(1, 2), 1, 2);
        assertPair(Pair.of(1, "2"), 1, "2");
        assertPair(Pair.of(array(1, 2)), 1, 2);
        assertPair(Pair.of(new AbstractMap.SimpleEntry<>(1, "2")), 1, "2");
    }
    
    @Test
    public void pair_simple_nulls() {
        assertPair(Pair.of(1, null), 1, null);
        assertPair(Pair.of(null, 2), null, 2);
        assertPair(Pair.of(null, null), null, null);

        assertPair(Pair.of(array(null, null)), null, null);
        assertPair(Pair.of(new AbstractMap.SimpleEntry<>(null, null)), null, null);
    }

    @Test
    public void pair_invalid() {
        assertThrows(AssertionError.class, () -> Pair.of(array()));
        assertThrows(AssertionError.class, () -> Pair.of(array(1)));
        assertThrows(AssertionError.class, () -> Pair.of(array(1, 2, 3)));
    }

    @Test
    public void pair_simple_swap() {
        assertPair(Pair.of(1, 2).swap(), 2, 1);
        assertPair(Pair.of(1, "2").swap(), "2", 1);
        assertPair(Pair.of(1, null).swap(), null, 1);
        assertPair(Pair.of(null, 2).swap(), 2, null);
        assertPair(Pair.of(null, null).swap(), null, null);
    }

    @Test
    public void pair_map() {
        assertPair(Pair.of(1, "2").map(x -> -x, String::length), -1, 1);
        assertPair(Pair.of(1, "2").mapFirst(x -> -x), -1, "2");
        assertPair(Pair.of(1, "2").mapSecond(String::length), 1, 1);
    }

    @Test
    public void pair_map_nullable() {
        assertPair(Pair.of(1, null).map(x -> -x, x -> x), -1, null);
        assertPair(Pair.of(1, null).map(x -> -x, String::valueOf), -1, "null");

        assertPair(Pair.of(null, 2).mapFirst(String::valueOf), "null", 2);
        assertPair(Pair.of(null, 2).mapSecond(String::valueOf), null, "2");

        assertPair(Pair.of(null, null).mapFirst(String::valueOf), "null", null);
        assertPair(Pair.of(null, null).mapSecond(String::valueOf), null, "null");
        assertPair(Pair.of(null, null).map(String::valueOf, String::valueOf), "null", "null");
    }

    @Test
    public void pair_mapToObj() {
        assertEquals(Pair.of("1", "2").mapToObj("%s%s"::formatted), "12");
        assertEquals(Pair.of("1", null).mapToObj("%s%s"::formatted), "1null");
        assertEquals(Pair.of(null, "2").mapToObj("%s%s"::formatted), "null2");
        assertEquals(Pair.of(null, null).mapToObj("%s%s"::formatted), "nullnull");
    }

    @Test
    public void pair_mapToInt() {
        assertEquals(Pair.of(1, 2).mapToInt(Integer::sum), 3);
        assertEquals(Pair.of(1, null).mapToInt(Objects::hashCode), Objects.hashCode(1, null));
        assertEquals(Pair.of(null, 2).mapToInt(Objects::hashCode), Objects.hashCode(null, 2));
        assertEquals(Pair.of(null, null).mapToInt(Objects::hashCode), Objects.hashCode(null, null));
    }

    @Test
    public void pair_mapToLong() {
        assertEquals(Pair.of(1, 2).mapToLong(Integer::sum), 3);
        assertEquals(Pair.of(1, null).mapToLong(Objects::hashCode), Objects.hashCode(1, null));
        assertEquals(Pair.of(null, 2).mapToLong(Objects::hashCode), Objects.hashCode(null, 2));
        assertEquals(Pair.of(null, null).mapToLong(Objects::hashCode), Objects.hashCode(null, null));
    }

    @Test
    public void pair_mapToDouble() {
        assertEquals(Pair.of(1, 2).mapToDouble(Integer::sum), 3);
        assertEquals(Pair.of(1, null).mapToDouble(Objects::hashCode), Objects.hashCode(1, null));
        assertEquals(Pair.of(null, 2).mapToDouble(Objects::hashCode), Objects.hashCode(null, 2));
        assertEquals(Pair.of(null, null).mapToDouble(Objects::hashCode), Objects.hashCode(null, null));
    }

    private static <U, V> void assertPair(@NotNull Pair<U, V> pair, @Nullable Object first, @Nullable Object second) {
        assertEquals(pair.first(), first);
        assertEquals(pair.getKey(), first);
        assertEquals(pair.second(), second);
        assertEquals(pair.getValue(), second);
    }
}
