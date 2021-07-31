package io.webby.db.kv.chronicle;

import io.webby.db.kv.KeyValueDb;
import net.openhft.chronicle.map.ChronicleMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ChronicleDb<K, V> implements KeyValueDb<K, V> {
    private final ChronicleMap<K, V> map;

    public ChronicleDb(@NotNull ChronicleMap<K, V> map) {
        this.map = map;
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        map.put(key, value);
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return map.get(key);
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
    public @NotNull V getOrDefault(@NotNull K key, @NotNull V def) {
        return map.getOrDefault(key, def);
    }

    @Override
    public @NotNull V getOrCompute(@NotNull K key, @NotNull Supplier<V> supplier) {
        return map.compute(key, (k, v) -> supplier.get());
    }

    @Override
    public @Nullable V remove(@NotNull K key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public long longSize() {
        return map.longSize();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        map.close();
    }
}
