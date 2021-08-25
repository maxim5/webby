package io.webby.db.kv.impl;

import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class DefaultKeyValueDb<K, V, M extends Map<K, V>> implements KeyValueDb<K, V> {
    protected final M map;

    public DefaultKeyValueDb(@NotNull M map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return map.get(key);
    }

    @Override
    public @NotNull V getOrDefault(@NotNull K key, @NotNull V def) {
        return map.getOrDefault(key, def);
    }

    @Override
    public boolean containsKey(@NotNull K key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        return map.containsValue(value);
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    @Override
    public @NotNull Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public @NotNull Collection<V> values() {
        return map.values();
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public @NotNull Map<K, V> asMap() {
        return map;
    }

    @Override
    public @NotNull Map<K, V> copyToMap() {
        return new HashMap<>(map);
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        map.put(key, value);
    }

    @Override
    public @Nullable V put(@NotNull K key, @NotNull V value) {
        return map.put(key, value);
    }

    @Override
    public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
        return map.putIfAbsent(key, value);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        this.map.putAll(map);
    }

    @Override
    public @Nullable V replace(@NotNull K key, @NotNull V value) {
        return map.replace(key, value);
    }

    @Override
    public boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public void delete(@NotNull K key) {
        map.remove(key);
    }

    @Override
    public @Nullable V remove(@NotNull K key) {
        return map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    public @NotNull M internalMap() {
        return map;
    }
}
