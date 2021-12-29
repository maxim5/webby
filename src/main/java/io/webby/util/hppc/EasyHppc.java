package io.webby.util.hppc;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntIntProcedure;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EasyHppc {
    public static @NotNull IntIntMap slice(@NotNull IntIntMap map, int @NotNull [] keys) {
        IntIntHashMap result = new IntIntHashMap(keys.length);
        for (int key : keys) {
            result.put(key, map.get(key));
        }
        return result;
    }

    public static @NotNull IntIntMap slice(@NotNull IntIntMap map, @NotNull IntContainer keys) {
        IntIntHashMap result = new IntIntHashMap(keys.size());
        for (IntCursor cursor : keys) {
            result.put(cursor.value, map.get(cursor.value));
        }
        return result;
    }

    public static @NotNull ArrayList<Integer> toJavaList(@NotNull IntContainer container) {
        ArrayList<Integer> list = new ArrayList<>(container.size());
        for (IntCursor cursor : container) {
            list.add(cursor.value);
        }
        return list;
    }

    public static @NotNull Map<Integer, Integer> toJavaMap(@NotNull IntIntMap map) {
        HashMap<Integer, Integer> result = new HashMap<>(map.size());
        map.forEach((IntIntProcedure) result::put);
        return result;
    }

    public static <T> @NotNull Map<Integer, T> toJavaMap(@NotNull IntObjectHashMap<T> map) {
        HashMap<Integer, T> result = new HashMap<>(map.size());
        map.forEach((IntObjectProcedure<T>) result::put);
        return result;
    }
}
