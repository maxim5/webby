package io.webby.testing;

import com.carrotsearch.hppc.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.testing.TestingBasics.array;
import static io.webby.util.base.EasyCast.castAny;

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

    public static @NotNull IntIntHashMap trim(@NotNull IntIntMap map) {
        IntIntHashMap copy = new IntIntHashMap(map);
        copy.removeAll((key, value) -> value == 0);
        return copy;
    }

    public static <T> @NotNull IntObjectMap<T> newIntObjectMap() {
        return new IntObjectHashMap<>();
    }

    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key, @NotNull T value) {
        return IntObjectHashMap.from(ints(key), array(value));
    }

    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key1, @NotNull T value1,
                                                               int key2, @NotNull T value2) {
        return IntObjectHashMap.from(ints(key1, key2), array(value1, value2));
    }

    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key1, @NotNull T value1,
                                                               int key2, @NotNull T value2,
                                                               int key3, @NotNull T value3) {
        return IntObjectHashMap.from(ints(key1, key2, key3), array(value1, value2, value3));
    }

    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(@NotNull Object @NotNull ... items) {
        assert items.length % 2 == 0 : "Invalid number of items: %d".formatted(items.length);
        IntObjectHashMap<T> map = new IntObjectHashMap<>();
        for (int i = 0; i < items.length; i += 2) {
            Integer key = (Integer) items[i];
            T value = castAny(items[i + 1]);
            map.put(key, value);
        }
        return map;
    }

    public static <T> @NotNull LongObjectMap<T> newLongObjectMap() {
        return new LongObjectHashMap<>();
    }

    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key, @NotNull T value) {
        return LongObjectHashMap.from(longs(key), array(value));
    }

    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key1, @NotNull T value1,
                                                                 long key2, @NotNull T value2) {
        return LongObjectHashMap.from(longs(key1, key2), array(value1, value2));
    }

    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key1, @NotNull T value1,
                                                                 long key2, @NotNull T value2,
                                                                 long key3, @NotNull T value3) {
        return LongObjectHashMap.from(longs(key1, key2, key3), array(value1, value2, value3));
    }

    public static @NotNull IntHashSet toIntSet(@Nullable Object obj) {
        if (obj instanceof int[] array) {
            return IntHashSet.from(array);
        }
        if (obj instanceof IntContainer container) {
            return new IntHashSet(container);
        }
        throw new IllegalArgumentException("Unrecognized object: " + obj);
    }

    public static int[] ints(int... values) {
        return values;
    }

    public static long[] longs(long... values) {
        return values;
    }
}
