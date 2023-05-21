package io.webby.util.hppc;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.LongCursor;
import com.carrotsearch.hppc.predicates.IntPredicate;
import com.carrotsearch.hppc.procedures.IntIntProcedure;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CheckReturnValue;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@CheckReturnValue
public class EasyHppc {
    public static @NotNull IntArrayList slice(@NotNull IntArrayList list, int fromIndex, int toIndex) {
        assert fromIndex >= 0 && toIndex >= 0 && fromIndex <= toIndex :
                "Invalid range: from=%d to=%d".formatted(fromIndex, toIndex);
        fromIndex = Math.min(fromIndex, list.elementsCount);
        toIndex = Math.min(toIndex, list.elementsCount);
        IntArrayList slice = new IntArrayList();
        slice.buffer = Arrays.copyOfRange(list.buffer, fromIndex, toIndex);
        slice.elementsCount = toIndex - fromIndex;
        return slice;
    }

    public static @NotNull LongArrayList slice(@NotNull LongArrayList list, int fromIndex, int toIndex) {
        assert fromIndex >= 0 && toIndex >= 0 && fromIndex <= toIndex :
            "Invalid range: from=%d to=%d".formatted(fromIndex, toIndex);
        fromIndex = Math.min(fromIndex, list.elementsCount);
        toIndex = Math.min(toIndex, list.elementsCount);
        LongArrayList slice = new LongArrayList();
        slice.buffer = Arrays.copyOfRange(list.buffer, fromIndex, toIndex);
        slice.elementsCount = toIndex - fromIndex;
        return slice;
    }

    public static <E extends Throwable> void iterateChunks(@NotNull IntContainer container,
                                                           int chunkSize,
                                                           @NotNull ThrowConsumer<IntArrayList, E> consumer) throws E {
        assert chunkSize > 0 : "Invalid chunk size: " + chunkSize;
        IntArrayList arrayList = toArrayList(container);
        for (int i = 0; i < arrayList.size(); i += chunkSize) {
            IntArrayList chunk = EasyHppc.slice(arrayList, i, i + chunkSize);
            consumer.accept(chunk);
        }
    }

    public static <E extends Throwable> void iterateChunks(@NotNull LongContainer container,
                                                           int chunkSize,
                                                           @NotNull ThrowConsumer<LongArrayList, E> consumer) throws E {
        assert chunkSize > 0 : "Invalid chunk size: " + chunkSize;
        LongArrayList arrayList = toArrayList(container);
        for (int i = 0; i < arrayList.size(); i += chunkSize) {
            LongArrayList chunk = EasyHppc.slice(arrayList, i, i + chunkSize);
            consumer.accept(chunk);
        }
    }

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

    public static @NotNull IntArrayList toArrayList(@NotNull IntContainer container) {
        return container instanceof IntArrayList arrayList ? arrayList : new IntArrayList(container);
    }

    public static @NotNull LongArrayList toArrayList(@NotNull LongContainer container) {
        return container instanceof LongArrayList arrayList ? arrayList : new LongArrayList(container);
    }

    public static @NotNull IntArrayList fromJavaIterableInt(@NotNull Iterable<Integer> list) {
        IntArrayList arrayList = new IntArrayList();
        list.forEach(arrayList::add);
        return arrayList;
    }

    public static @NotNull LongArrayList fromJavaIterableLong(@NotNull Iterable<Long> list) {
        LongArrayList arrayList = new LongArrayList();
        list.forEach(arrayList::add);
        return arrayList;
    }

    public static @NotNull Stream<IntCursor> toJavaStream(@NotNull IntContainer container) {
        return Streams.stream(container);
    }

    public static @NotNull Stream<LongCursor> toJavaStream(@NotNull LongContainer container) {
        return Streams.stream(container);
    }

    public static @NotNull ArrayList<Integer> toJavaList(@NotNull IntContainer container) {
        ArrayList<Integer> list = new ArrayList<>(container.size());
        for (IntCursor cursor : container) {
            list.add(cursor.value);
        }
        return list;
    }

    public static @NotNull ArrayList<Long> toJavaList(@NotNull LongContainer container) {
        ArrayList<Long> list = new ArrayList<>(container.size());
        for (LongCursor cursor : container) {
            list.add(cursor.value);
        }
        return list;
    }

    public static @NotNull HashMap<Integer, Integer> toJavaMap(@NotNull IntIntMap map) {
        HashMap<Integer, Integer> result = new HashMap<>(map.size());
        map.forEach((IntIntProcedure) result::put);
        return result;
    }

    public static <T> @NotNull Map<Integer, T> toJavaMap(@NotNull IntObjectMap<T> map) {
        HashMap<Integer, T> result = new HashMap<>(map.size());
        map.forEach((IntObjectProcedure<T>) result::put);
        return result;
    }

    public static @NotNull IntArrayList collectFromIntStream(@NotNull IntStream stream) {
        return stream.collect(IntArrayList::new, IntArrayList::add, IntArrayList::addAll);
    }

    public static @NotNull IntHashSet union(@NotNull IntContainer lhs, @NotNull IntContainer rhs) {
        IntHashSet copy = new IntHashSet(lhs);
        copy.addAll(rhs);
        return copy;
    }

    public static @NotNull IntHashSet intersect(@NotNull IntContainer lhs, @NotNull IntLookupContainer rhs) {
        IntHashSet copy = new IntHashSet(lhs);
        copy.retainAll(rhs);
        return copy;
    }

    public static @NotNull IntHashSet subtract(@NotNull IntContainer lhs, @NotNull IntContainer rhs) {
        IntHashSet copy = new IntHashSet(lhs);
        copy.removeAll(rhs);
        return copy;
    }

    public static @NotNull IntHashSet retainAllCopy(@NotNull IntContainer container, @NotNull IntPredicate predicate) {
        IntHashSet copy = new IntHashSet(container);
        copy.retainAll(predicate);
        return copy;
    }

    public static @NotNull IntHashSet removeAllCopy(@NotNull IntContainer container, @NotNull IntPredicate predicate) {
        IntHashSet copy = new IntHashSet(container);
        copy.removeAll(predicate);
        return copy;
    }

    public static int computeIfAbsent(@NotNull IntIntMap map, int key, @NotNull IntSupplier def) {
        int result = map.get(key);
        if (result == 0 && !map.containsKey(key)) {
            result = def.getAsInt();
            map.put(key, result);
        }
        return result;
    }

    public static <T> T computeIfAbsent(@NotNull IntObjectMap<T> map, int key, @NotNull Supplier<T> def) {
        T result = map.get(key);
        if (result == null && !map.containsKey(key)) {
            result = def.get();
            map.put(key, result);
        }
        return result;
    }
}
