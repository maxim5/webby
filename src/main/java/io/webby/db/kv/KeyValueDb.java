package io.webby.db.kv;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

// More Map methods: compute
public interface KeyValueDb<K, V> extends Closeable {
    int size();

    default long longSize() {
        return size();
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean isNotEmpty() {
        return size() > 0;
    }

    @Nullable V get(@NotNull K key);

    default @NotNull V getOrDefault(@NotNull K key, @NotNull V def) {
        V value = get(key);
        return value != null ? value : def;
    }

    default @NotNull V getOrCompute(@NotNull K key, @NotNull Supplier<V> supplier) {
        V value = get(key);
        return value != null ? value : supplier.get();
    }

    default boolean containsKey(@NotNull K key) {
        return get(key) != null;
    }

    boolean containsValue(@NotNull V value);

    @NotNull Set<K> keySet();

    void set(@NotNull K key, @NotNull V value);

    default @Nullable V put(@NotNull K key, @NotNull V value) {
        V existing = get(key);
        set(key, value);
        return existing;
    }

    default @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
        V existing = get(key);
        if (existing == null) {
            set(key, value);
        }
        return existing;
    }

    default @Nullable V putIfPresent(@NotNull K key, @NotNull V value) {
        return replace(key, value);
    }

    default void putAll(@NotNull Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    default @Nullable V replace(@NotNull K key, @NotNull V value) {
        V curValue;
        if (((curValue = get(key)) != null) || containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
    }

    default boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) || (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    void delete(@NotNull K key);

    default @Nullable V remove(@NotNull K key) {
        V existing = get(key);
        delete(key);
        return existing;
    }

    void clear();

    void flush();

    default void forceFlush() {
        flush();
    }

    void close();
}
