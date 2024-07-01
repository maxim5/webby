package io.spbx.util.testing;

import com.carrotsearch.hppc.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.spbx.util.base.EasyCast.castAny;
import static io.spbx.util.base.EasyExceptions.newIllegalArgumentException;
import static io.spbx.util.testing.TestingBasics.array;
import static io.spbx.util.testing.TestingPrimitives.ints;
import static io.spbx.util.testing.TestingPrimitives.longs;
import static java.util.Objects.requireNonNull;

public class TestingHppc {
    public static @NotNull IntIntHashMap newIntMap(int key, int value) {
        return IntIntHashMap.from(ints(key), ints(value));
    }

    public static @NotNull IntIntHashMap newIntMap(int key1, int value1, int key2, int value2) {
        return IntIntHashMap.from(ints(key1, key2), ints(value1, value2));
    }

    public static @NotNull IntIntHashMap newIntMap(int... keyValues) {
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

    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key, @Nullable T value) {
        return IntObjectHashMap.from(ints(key), array(value));
    }

    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key1, @Nullable T value1,
                                                               int key2, @Nullable T value2) {
        return IntObjectHashMap.from(ints(key1, key2), array(value1, value2));
    }

    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key1, @Nullable T value1,
                                                               int key2, @Nullable T value2,
                                                               int key3, @Nullable T value3) {
        return IntObjectHashMap.from(ints(key1, key2, key3), array(value1, value2, value3));
    }

    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(@Nullable Object @NotNull ... items) {
        assert items.length % 2 == 0 : "Invalid number of items: %d".formatted(items.length);
        IntObjectHashMap<T> map = new IntObjectHashMap<>();
        for (int i = 0; i < items.length; i += 2) {
            Integer key = (Integer) requireNonNull(items[i]);
            T value = castAny(items[i + 1]);
            map.put(key, value);
        }
        return map;
    }

    public static @NotNull LongLongHashMap newLongMap(long key, long value) {
        return LongLongHashMap.from(longs(key), longs(value));
    }

    public static @NotNull LongLongHashMap newLongMap(long key1, long value1, long key2, long value2) {
        return LongLongHashMap.from(longs(key1, key2), longs(value1, value2));
    }

    public static @NotNull LongLongHashMap newLongMap(long... keyValues) {
        assert keyValues.length % 2 == 0 : "Invalid number of items: %d".formatted(keyValues.length);
        long[] keys = new long[keyValues.length / 2];
        long[] values = new long[keyValues.length / 2];
        for (int i = 0; i < keyValues.length; i += 2) {
            keys[i / 2] = keyValues[i];
            values[i / 2] = keyValues[i + 1];
        }
        return LongLongHashMap.from(keys, values);
    }

    public static @NotNull LongLongHashMap trim(@NotNull LongLongMap map) {
        LongLongHashMap copy = new LongLongHashMap(map);
        copy.removeAll((key, value) -> value == 0);
        return copy;
    }

    public static <T> @NotNull LongObjectMap<T> newLongObjectMap() {
        return new LongObjectHashMap<>();
    }

    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key, @Nullable T value) {
        return LongObjectHashMap.from(longs(key), array(value));
    }

    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key1, @Nullable T value1,
                                                                 long key2, @Nullable T value2) {
        return LongObjectHashMap.from(longs(key1, key2), array(value1, value2));
    }

    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key1, @Nullable T value1,
                                                                 long key2, @Nullable T value2,
                                                                 long key3, @Nullable T value3) {
        return LongObjectHashMap.from(longs(key1, key2, key3), array(value1, value2, value3));
    }

    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(@Nullable Object @NotNull ... items) {
        assert items.length % 2 == 0 : "Invalid number of items: %d".formatted(items.length);
        LongObjectHashMap<T> map = new LongObjectHashMap<>();
        for (int i = 0; i < items.length; i += 2) {
            Long key = (Long) requireNonNull(items[i]);
            T value = castAny(items[i + 1]);
            map.put(key, value);
        }
        return map;
    }

    public static @NotNull IntHashSet toIntSet(@Nullable Object obj) {
        if (obj instanceof int[] array) {
            return IntHashSet.from(array);
        }
        if (obj instanceof IntContainer container) {
            return new IntHashSet(container);
        }
        throw newIllegalArgumentException("Unrecognized object: %s", obj);
    }
}
