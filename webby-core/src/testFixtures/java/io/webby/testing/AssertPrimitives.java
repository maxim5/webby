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
    public static @NotNull IntContainerSubject assertThat(@NotNull IntContainer container) {
        return new IntContainerSubject(container);
    }

    public static @NotNull LongContainerSubject assertThat(@NotNull LongContainer container) {
        return new LongContainerSubject(container);
    }

    public static @NotNull IntIntMapSubject assertMap(@NotNull IntIntMap map) {
        return new IntIntMapSubject(map);
    }

    public static <T> @NotNull IntObjectMapSubject<T> assertMap(@NotNull IntObjectMap<T> map) {
        return new IntObjectMapSubject<>(map);
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
        public void isEmpty() {
            Truth.assertThat(map).isEmpty();
        }

        public void isEqualTo(@NotNull IntIntMap expected) {
            Truth.assertThat(map).isEqualTo(expected);
        }

        public void isEqualTo(@NotNull Map<Integer, Integer> expected) {
            asJavaMap().isEqualTo(expected);
        }

        public void containsExactly(int key, int value) {
            assertEquals(newIntMap(key, value), map);
        }

        public void containsExactly(int key1, int value1, int key2, int value2) {
            assertEquals(newIntMap(key1, value1, key2, value2), map);
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

    public record IntObjectMapSubject<T>(@NotNull IntObjectMap<T> map) {
        public void isEmpty() {
            Truth.assertThat(map).isEmpty();
        }

        public void isEqualTo(@NotNull IntObjectMap<T> expected) {
            Truth.assertThat(map).isEqualTo(expected);
        }

        public void isEqualTo(@NotNull Map<Integer, T> expected) {
            asJavaMap().isEqualTo(expected);
        }

        public void containsExactly(int key, @Nullable T expectedValue) {
            isEqualTo(newIntObjectMap(key, expectedValue));
        }

        public void containsExactly(int key1, @Nullable T expectedValue1, int key2, @Nullable T expectedValue2) {
            isEqualTo(newIntObjectMap(key1, expectedValue1, key2, expectedValue2));
        }

        public void containsExactly(@Nullable Object @NotNull ... expectedKeysValues) {
            isEqualTo(newIntObjectMap(expectedKeysValues));
        }

        public @NotNull MapSubject asJavaMap() {
            return Truth.assertThat(toJavaMap(map));
        }
    }
}
