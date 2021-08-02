package io.webby.db.kv.mapdb;

import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.DB;
import org.mapdb.HTreeMap;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MapDbImpl<K, V> implements KeyValueDb<K, V> {
    private final DB db;
    private final HTreeMap<K, V> map;

    public MapDbImpl(@NotNull DB db, @NotNull HTreeMap<K, V> map) {
        this.db = db;
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public long longSize() {
        return map.sizeLong();
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
    public @NotNull V getOrCompute(@NotNull K key, @NotNull Supplier<V> supplier) {
        return map.compute(key, (k, v) -> supplier.get());
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
    public @Nullable V remove(@NotNull K key) {
        return map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public void flush() {
        db.commit();
    }

    @Override
    public void close() {
        db.close();
    }
}
