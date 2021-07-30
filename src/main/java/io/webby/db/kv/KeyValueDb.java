package io.webby.db.kv;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.function.Supplier;

// remove, replace, compute
public interface KeyValueDb<K, V> extends Closeable {
    void set(@NotNull K key, @NotNull V value);

    @Nullable V get(@NotNull K key);

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

    default @NotNull V getOrDefault(@NotNull K key, @NotNull V def) {
        V value = get(key);
        return value != null ? value : def;
    }

    default @NotNull V getOrCompute(@NotNull K key, @NotNull Supplier<V> supplier) {
        V value = get(key);
        return value != null ? value : supplier.get();
    }

    void flush();

    void close();
}
