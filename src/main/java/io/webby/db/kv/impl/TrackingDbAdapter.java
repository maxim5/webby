package io.webby.db.kv.impl;

import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static io.webby.db.kv.impl.DbStatsListener.Op;
import static io.webby.db.kv.impl.DbStatsListener.OpContext;

public class TrackingDbAdapter<K, V> implements KeyValueDb<K, V> {
    private final KeyValueDb<K, V> delegate;
    private final DbStatsListener listener;

    public TrackingDbAdapter(@NotNull KeyValueDb<K, V> delegate, @NotNull DbStatsListener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public int size() {
        try (OpContext ignored = listener.report(Op.SIZE)) {
            return delegate.size();
        }
    }

    @Override
    public long longSize() {
        try (OpContext ignored = listener.report(Op.SIZE)) {
            return delegate.longSize();
        }
    }

    @Override
    public boolean isEmpty() {
        try (OpContext ignored = listener.report(Op.SIZE)) {
            return delegate.isEmpty();
        }
    }

    @Override
    public boolean isNotEmpty() {
        try (OpContext ignored = listener.report(Op.SIZE)) {
            return delegate.isNotEmpty();
        }
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Op.GET, key)) {
            return delegate.get(key);
        }
    }

    @Override
    public @NotNull V getOrDefault(@NotNull K key, @NotNull V def) {
        try (OpContext ignored = listener.reportKey(Op.GET, key)) {
            return delegate.getOrDefault(key, def);
        }
    }

    @Override
    public @NotNull V getOrCompute(@NotNull K key, @NotNull Supplier<V> supplier) {
        try (OpContext ignored = listener.reportKey(Op.GET, key)) {
            return delegate.getOrCompute(key, supplier);
        }
    }

    @Override
    public @NotNull Optional<V> getOptional(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Op.GET, key)) {
            return delegate.getOptional(key);
        }
    }

    @Override
    public boolean containsKey(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Op.GET, key)) {
            return delegate.containsKey(key);
        }
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        try (OpContext ignored = listener.report(Op.SCAN)) {
            return delegate.containsValue(value);
        }
    }

    @Override
    public @NotNull Iterable<K> keys() {
        try (OpContext ignored = listener.report(Op.SCAN)) {
            return delegate.keys();
        }
    }

    @Override
    public @NotNull Set<K> keySet() {
        try (OpContext ignored = listener.report(Op.SCAN)) {
            return delegate.keySet();
        }
    }

    @Override
    public @NotNull Collection<V> values() {
        try (OpContext ignored = listener.report(Op.SCAN)) {
            return delegate.values();
        }
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        try (OpContext ignored = listener.report(Op.SCAN)) {
            return delegate.entrySet();
        }
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKeyValue(Op.SET, key, value)) {
            delegate.set(key, value);
        }
    }

    @Override
    public @Nullable V put(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKeyValue(Op.SET, key, value)) {
            return delegate.put(key, value);
        }
    }

    @Override
    public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKeyValue(Op.SET, key, value)) {
            return delegate.putIfAbsent(key, value);
        }
    }

    @Override
    public @Nullable V putIfPresent(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKeyValue(Op.SET, key, value)) {
            return delegate.putIfPresent(key, value);
        }
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        try (OpContext ignored = listener.context()) {
            map.forEach((key, value) -> listener.reportKeyValue(Op.SET, key, value));
            delegate.putAll(map);
        }
    }

    @Override
    public @Nullable V replace(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKeyValue(Op.SET, key, value)) {
            return delegate.replace(key, value);
        }
    }

    @Override
    public boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue) {
        try (OpContext ignored = listener.reportKeyValue(Op.SET, key, newValue)) {
            return delegate.replace(key, oldValue, newValue);
        }
    }

    @Override
    public void delete(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Op.DELETE, key)) {
            delegate.delete(key);
        }
    }

    @Override
    public @Nullable V remove(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Op.DELETE, key)) {
            return delegate.remove(key);
        }
    }

    @Override
    public void clear() {
        try (OpContext ignored = listener.report(Op.DELETE)) {
            delegate.clear();
        }
    }

    @Override
    public @NotNull Map<K, V> asMap() {
        try (OpContext ignored = listener.report(Op.SCAN)) {
            return delegate.asMap();
        }
    }

    @Override
    public @NotNull Map<K, V> copyToMap() {
        try (OpContext ignored = listener.report(Op.SCAN)) {
            return delegate.copyToMap();
        }
    }

    @Override
    public void flush() {
        try (OpContext ignored = listener.report(Op.IO)) {
            delegate.flush();
        }
    }

    @Override
    public void forceFlush() {
        try (OpContext ignored = listener.report(Op.IO)) {
            delegate.forceFlush();
        }
    }

    @Override
    public void close() {
        try (OpContext ignored = listener.report(Op.IO)) {
            delegate.close();
        }
    }
}
