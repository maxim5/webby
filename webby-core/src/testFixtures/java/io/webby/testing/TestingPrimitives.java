package io.webby.testing;

import com.carrotsearch.hppc.*;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class TestingPrimitives {
    public static @NotNull IntIntHashMap newIntMap(int key, int value) {
        return IntIntHashMap.from(ints(key), ints(value));
    }

    public static @NotNull IntIntHashMap newIntMap(int key1, int value1, int key2, int value2) {
        return IntIntHashMap.from(ints(key1, key2), ints(value1, value2));
    }

    public static @NotNull IntIntHashMap newIntMap(int ... keyValues) {
        assert keyValues.length % 2 == 0 : "Invalid number of items: %d".formatted(keyValues.length);
        int[] keys = new int[keyValues.length / 2];
        int[] values = new int[keyValues.length / 2];
        for (int i = 0; i < keyValues.length; i += 2) {
            keys[i / 2] = keyValues[i];
            values[i / 2] = keyValues[i + 1];
        }
        return IntIntHashMap.from(keys, values);
    }

    public static @NotNull IntIntHashMap trim(@NotNull IntIntHashMap map) {
        IntIntHashMap clone = map.clone();
        clone.removeAll((key, value) -> value == 0);
        return clone;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key, @NotNull T value) {
        return IntObjectHashMap.from(ints(key), array(value));
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key1, @NotNull T value1,
                                                               int key2, @NotNull T value2) {
        return IntObjectHashMap.from(ints(key1, key2), array(value1, value2));
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key, @NotNull T value) {
        return LongObjectHashMap.from(longs(key), array(value));
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key1, @NotNull T value1,
                                                                 long key2, @NotNull T value2) {
        return LongObjectHashMap.from(longs(key1, key2), array(value1, value2));
    }

    public static int[] ints(int... values) {
        return values;
    }

    public static long[] longs(long... values) {
        return values;
    }
}
