package io.spbx.util.collect;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.spbx.util.base.EasyCast.castAny;

public class EasyMaps {
    public static <K, V> @NotNull Map<K, V> mutableMap() {
        return new HashMap<>();
    }

    public static <K, V> @NotNull Map<K, V> mutableMap(int size) {
        return new HashMap<>(size);
    }

    public static <K, V> @NotNull Map<K, V> orderedMap() {
        return new LinkedHashMap<>();
    }

    public static <K, V> @NotNull Map<K, V> orderedMap(int size) {
        return new LinkedHashMap<>(size);
    }

    public static <K, V> @NotNull Map<K, V> concurrentMap() {
        return new ConcurrentHashMap<>();
    }

    public static <K, V> @NotNull Map<K, V> concurrentMap(int size) {
        return new ConcurrentHashMap<>(size);
    }

    @Pure
    public static <K, V> @NotNull LinkedHashMap<K, V> asMap(@Nullable Object @NotNull ... items) {
        return asMap(Arrays.asList(items));
    }

    @Pure
    public static <K, V> @NotNull LinkedHashMap<K, V> asMap(@NotNull List<?> items) {
        assert items.size() % 2 == 0 : "Invalid number of items: %d".formatted(items.size());
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i += 2) {
            V previous = result.put(castAny(items.get(i)), castAny(items.get(i + 1)));
            assert previous == null : "Multiple entries with the same key: " + items.get(i);
        }
        return result;
    }

    @Pure
    public static <K, V> @NotNull ImmutableMap<V, K> inverseMap(@NotNull Map<K, V> map) {
        return map.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    @Pure
    public static <K, V> @NotNull ImmutableMap<K, V> immutableOf(@NotNull K k1, @NotNull V v1,
                                                                 @NotNull Object @NotNull ... items) {
        assert items.length % 2 == 0 : "Invalid number of items: %d".formatted(items.length);
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(1 + items.length >> 1);
        builder.put(k1, v1);
        for (int i = 0; i < items.length; i += 2) {
            builder.put(castAny(items[i]), castAny(items[i + 1]));
        }
        return builder.build();
    }

    @Pure
    public static <K, V> @NotNull Map<K, V> merge(@NotNull Map<K, V> map1, @NotNull Map<K, V> map2) {
        LinkedHashMap<K, V> result = new LinkedHashMap<>(map1);
        result.putAll(map2);
        return result;
    }

    @Pure
    public static <K, V> @NotNull Map<K, V> merge(@NotNull Map<K, V> map1,
                                                  @NotNull Map<K, V> map2,
                                                  @NotNull Map<K, V> map3) {
        LinkedHashMap<K, V> result = new LinkedHashMap<>(map1);
        result.putAll(map2);
        result.putAll(map3);
        return result;
    }

    @Pure
    public static <K, V> @NotNull ImmutableMap<K, V> mergeToImmutable(@NotNull Map<K, V> map1, @NotNull Map<K, V> map2) {
        return ImmutableMap.<K, V>builder().putAll(map1).putAll(map2).buildOrThrow();
    }

    @Pure
    public static <K, V> @NotNull ImmutableMap<K, V> mergeToImmutable(@NotNull Map<K, V> map1,
                                                                      @NotNull Map<K, V> map2,
                                                                      @NotNull Map<K, V> map3) {
        return ImmutableMap.<K, V>builder().putAll(map1).putAll(map2).putAll(map3).buildOrThrow();
    }
}
