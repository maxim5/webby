package io.webby.testing;

import com.carrotsearch.hppc.*;
import com.google.common.truth.MapSubject;
import com.google.common.truth.Truth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.webby.testing.TestingPrimitives.*;
import static io.webby.util.hppc.EasyHppc.toArrayList;
import static io.webby.util.hppc.EasyHppc.toJavaMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertPrimitives {

    public static <T> void assertMap(@NotNull IntObjectMap<T> map, @Nullable Object @NotNull ... expectedKeysValues) {
        assertEquals(newIntObjectMap(expectedKeysValues), map);
    }

    public static @NotNull IntContainerSubject assertThat(@NotNull IntContainer container) {
        return new IntContainerSubject(container);
    }

    public static @NotNull LongContainerSubject assertThat(@NotNull LongContainer container) {
        return new LongContainerSubject(container);
    }

    public static @NotNull IntIntMapSubject assertMap(@NotNull IntIntMap map) {
        return new IntIntMapSubject(map);
    }

    public record IntContainerSubject(@NotNull IntContainer container) {
        public void isEmpty() {
            Truth.assertThat(container).isEmpty();
        }

        public void containsExactlyInOrder(int... expected) {
            Truth.assertThat(toArrayList(container)).isEqualTo(IntArrayList.from(expected));
        }

        public void containsExactlyNoOrder(int... expected) {
            Truth.assertThat(new IntHashSet(container)).isEqualTo(IntHashSet.from(expected));
        }
    }

    public record LongContainerSubject(@NotNull LongContainer container) {
        public void isEmpty() {
            Truth.assertThat(container).isEmpty();
        }

        public void containsExactlyInOrder(long... expected) {
            Truth.assertThat(toArrayList(container)).isEqualTo(LongArrayList.from(expected));
        }

        public void containsExactlyNoOrder(long... expected) {
            Truth.assertThat(new LongHashSet(container)).isEqualTo(LongHashSet.from(expected));
        }
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
