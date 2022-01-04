package io.webby.testing;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntMap;
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
}
