package io.webby.db.kv.swaydb;

import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import swaydb.java.Map;

import java.util.*;

public class SwayDb<K, V> implements KeyValueDb<K, V> {
    private final Map<K, V, Void> map;

    public SwayDb(@NotNull Map<K, V, Void> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.count();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean isNotEmpty() {
        return map.nonEmpty();
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return map.get(key).orElse(null);
    }

    @Override
    public @NotNull Optional<V> getOptional(@NotNull K key) {
        return map.get(key);
    }

    @Override
    public boolean containsKey(@NotNull K key) {
        return map.contains(key);
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        return map.asJava().containsValue(value);
    }

    @Override
    public @NotNull List<K> keys() {
        return map.keys().materialize();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return map.asJava().keySet();
    }

    @Override
    public @NotNull List<V> values() {
        return map.values().materialize();
    }

    @Override
    public @NotNull Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.asJava().entrySet();
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        map.put(key, value);
    }

    @Override
    public void delete(@NotNull K key) {
        map.remove(key);
    }

    @Override
    public void clear() {
        // map.delete();
        map.clearKeyValues();
    }

    @Override
    public @NotNull java.util.Map<K, V> asMap() {
        return map.asJava();
    }

    @Override
    public @NotNull java.util.Map<K, V> copyToMap() {
        return new HashMap<>(map.asJava());
    }

    public @NotNull Map<K, V, Void> internalMap() {
        return map;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        map.close();
    }
}
