package io.webby.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BiKeyMap<K1, K2, V> {
    private final Map<K1, V> map1;
    private final Map<K2, V> map2;

    /*package*/ BiKeyMap(@NotNull Map<K1, V> map1, @NotNull Map<K2, V> map2) {
        assert map1.isEmpty() : "Map must be empty, but got: %s".formatted(map1);
        assert map2.isEmpty() : "Map must be empty, but got: %s".formatted(map2);
        this.map1 = map1;
        this.map2 = map2;
    }

    public static <K1, K2, V> @NotNull BiKeyMap<K1, K2, V> createHashMap() {
        return new BiKeyMap<>(new HashMap<>(), new HashMap<>());
    }

    public static <K1, K2, V> @NotNull BiKeyMap<K1, K2, V> createLinkedHashMap() {
        return new BiKeyMap<>(new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    public static <K1, K2, V> @NotNull BiKeyMap<K1, K2, V> createWeakHashMap() {
        return new BiKeyMap<>(new WeakHashMap<>(), new WeakHashMap<>());
    }

    public static <K1, K2, V> @NotNull BiKeyMap<K1, K2, V> createConcurrentHashMap() {
        return new BiKeyMap<>(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    public static <K1 extends Comparable<K1>, K2 extends Comparable<K2>, V> @NotNull BiKeyMap<K1, K2, V> createTreeMap() {
        return new BiKeyMap<>(new TreeMap<>(), new TreeMap<>());
    }

    public @Nullable V get1(@NotNull K1 key) {
        return map1.get(key);
    }

    public @Nullable V get2(@NotNull K2 key) {
        return map2.get(key);
    }

    public @Nullable V put(@NotNull K1 key1, @NotNull K2 key2, @NotNull V value) {
        V old1 = map1.put(key1, value);
        V old2 = map2.put(key2, value);
        assert Objects.equals(old1, old2) :
                "Internal inconsistency: key1=%s, key2=%s, oldVal1=%s, oldVal2=%s".formatted(key1, key2, old1, old2);
        return old1;
    }
}
