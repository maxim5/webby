package io.webby.testing;

import com.carrotsearch.hppc.*;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingPrimitives.newIntMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertPrimitives {
    public static void assertIntsNoOrder(@NotNull IntContainer container, int... expected) {
        assertEquals(IntHashSet.from(expected), new IntHashSet(container));
    }

    public static void assertIntsOrdered(@NotNull IntContainer container, int... expected) {
        assertEquals(IntArrayList.from(expected), container);
    }

    public static void assertInts(@NotNull IntIntMap map, int... keyValues) {
        assertEquals(newIntMap(keyValues), map);
    }

    public static void assertLongsNoOrder(@NotNull LongContainer container, long... expected) {
        assertEquals(LongHashSet.from(expected), new LongHashSet(container));
    }

    public static void assertLongsOrdered(@NotNull LongContainer container, long... expected) {
        assertEquals(LongArrayList.from(expected), container);
    }
}
