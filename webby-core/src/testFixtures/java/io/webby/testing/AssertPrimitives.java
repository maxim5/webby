package io.webby.testing;

import com.carrotsearch.hppc.*;
import com.google.common.truth.MapSubject;
import com.google.common.truth.Truth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.webby.testing.TestingPrimitives.*;
import static io.webby.util.hppc.EasyHppc.toJavaMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertPrimitives {
    public static void assertIntsNoOrder(@NotNull IntContainer container, int... expected) {
        assertEquals(IntHashSet.from(expected), new IntHashSet(container));
    }

    public static void assertIntsOrdered(@NotNull IntContainer container, int... expected) {
        assertEquals(IntArrayList.from(expected), container);
    }

    public static <T> void assertMap(@NotNull IntObjectMap<T> map, @Nullable Object @NotNull ... expectedKeysValues) {
        assertEquals(newIntObjectMap(expectedKeysValues), map);
    }

    public static void assertLongsNoOrder(@NotNull LongContainer container, long... expected) {
        assertEquals(LongHashSet.from(expected), new LongHashSet(container));
    }

    public static void assertLongsOrdered(@NotNull LongContainer container, long... expected) {
        assertEquals(LongArrayList.from(expected), container);
    }

    public static @NotNull IntIntMapSubject assertMap(@NotNull IntIntMap map) {
        return new IntIntMapSubject(map);
    }

    public record IntIntMapSubject(@NotNull IntIntMap map) {
        public void isEqualTo(@NotNull IntIntMap expected) {
            Truth.assertThat(map).isEqualTo(expected);
        }

        public void isEqualTo(@NotNull Map<Integer, Integer> expected) {
            asJavaMap().isEqualTo(expected);
        }

        public void containsExactly(int... expectedKeysValues) {
            assertEquals(newIntMap(expectedKeysValues), map);
        }

        public void containsExactlyTrimmed(int... expectedKeysValues) {
            isEqualTo(trim(newIntMap(expectedKeysValues)));
        }

        public @NotNull IntIntMapSubject trimmed() {
            return new IntIntMapSubject(trim(map));
        }

        public @NotNull MapSubject asJavaMap() {
            return Truth.assertThat(toJavaMap(map));
        }
    }
}
