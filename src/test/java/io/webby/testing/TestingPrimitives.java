package io.webby.testing;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.LongObjectHashMap;
import com.carrotsearch.hppc.LongObjectMap;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class TestingPrimitives {
    @SuppressWarnings("unchecked")
    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key, @NotNull T value) {
        return IntObjectHashMap.from(new int[] { key }, array(value));
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull IntObjectMap<T> newIntObjectMap(int key1, @NotNull T value1,
                                                               int key2, @NotNull T value2) {
        return IntObjectHashMap.from(new int[] { key1, key2 }, array(value1, value2));
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key, @NotNull T value) {
        return LongObjectHashMap.from(new long[] { key }, array(value));
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull LongObjectMap<T> newLongObjectMap(long key1, @NotNull T value1,
                                                                 long key2, @NotNull T value2) {
        return LongObjectHashMap.from(new long[] { key1, key2 }, array(value1, value2));
    }
}
