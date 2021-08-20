package io.webby.db.kv;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

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
    public @NotNull Set<K> keySet() {
        return map.keySet();
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
}
