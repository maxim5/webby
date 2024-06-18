package io.spbx.webby.db.kv.impl;

import com.google.common.collect.Streams;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.perf.stats.DbStatsListener;
import io.spbx.webby.perf.stats.Stat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.spbx.webby.perf.stats.DbStatsListener.OpContext;

public class TrackingDbAdapter<K, V> implements KeyValueDb<K, V> {
    private final KeyValueDb<K, V> delegate;
    private final DbStatsListener listener;

    public TrackingDbAdapter(@NotNull KeyValueDb<K, V> delegate, @NotNull DbStatsListener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public int size() {
        try (OpContext ignored = listener.report(Stat.DB_SIZE)) {
            return delegate.size();
        }
    }

    @Override
    public long longSize() {
        try (OpContext ignored = listener.report(Stat.DB_SIZE)) {
            return delegate.longSize();
        }
    }

    @Override
    public boolean isEmpty() {
        try (OpContext ignored = listener.report(Stat.DB_SIZE)) {
            return delegate.isEmpty();
        }
    }

    @Override
    public boolean isNotEmpty() {
        try (OpContext ignored = listener.report(Stat.DB_SIZE)) {
            return delegate.isNotEmpty();
        }
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Stat.DB_GET, key)) {
            return delegate.get(key);
        }
    }

    @Override
    public @NotNull V getOrDefault(@NotNull K key, @NotNull V def) {
        try (OpContext ignored = listener.reportKey(Stat.DB_GET, key)) {
            return delegate.getOrDefault(key, def);
        }
    }

    @Override
    public @NotNull V getOrCompute(@NotNull K key, @NotNull Supplier<V> supplier) {
        try (OpContext ignored = listener.reportKey(Stat.DB_GET, key)) {
            return delegate.getOrCompute(key, supplier);
        }
    }

    @Override
    public @NotNull Optional<V> getOptional(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Stat.DB_GET, key)) {
            return delegate.getOptional(key);
        }
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull K @NotNull [] keys) {
        try (OpContext ignored = listener.reportKeys(Stat.DB_GET, keys)) {
            return delegate.getAll(keys);
        }
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull Iterable<K> keys) {
        try (OpContext ignored = listener.reportKeys(Stat.DB_GET, keys)) {
            return delegate.getAll(keys);
        }
    }

    @Override
    public boolean containsKey(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Stat.DB_GET, key)) {
            return delegate.containsKey(key);
        }
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        try (OpContext ignored = listener.report(Stat.DB_SCAN)) {
            return delegate.containsValue(value);
        }
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        try (OpContext ignored = listener.report(Stat.DB_SCAN)) {
            delegate.forEach(action);
        }
    }

    @Override
    public @NotNull Iterator<Map.Entry<K, V>> iterator() {
        try (OpContext ignored = listener.report(Stat.DB_SCAN)) {
            return delegate.iterator();
        }
    }

    @Override
    public @NotNull Iterable<K> keys() {
        try (OpContext ignored = listener.report(Stat.DB_SCAN)) {
            return delegate.keys();
        }
    }

    @Override
    public @NotNull Set<K> keySet() {
        try (OpContext ignored = listener.report(Stat.DB_SCAN)) {
            return delegate.keySet();
        }
    }

    @Override
    public @NotNull Collection<V> values() {
        try (OpContext ignored = listener.report(Stat.DB_SCAN)) {
            return delegate.values();
        }
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        try (OpContext ignored = listener.report(Stat.DB_SCAN)) {
            return delegate.entrySet();
        }
    }

    @Override
    public @NotNull Map<K, V> asMap() {
        try (OpContext ignored = listener.report(Stat.DB_SCAN)) {
            return delegate.asMap();
        }
    }

    @Override
    public @NotNull Map<K, V> copyToMap() {
        try (OpContext ignored = listener.report(Stat.DB_SCAN)) {
            return delegate.copyToMap();
        }
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            delegate.set(key, value);
        }
    }

    @Override
    public @Nullable V put(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            return delegate.put(key, value);
        }
    }

    @Override
    public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            return delegate.putIfAbsent(key, value);
        }
    }

    @Override
    public @Nullable V putIfPresent(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            return delegate.putIfPresent(key, value);
        }
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        try (OpContext ignored = listener.reportKeys(Stat.DB_SET, map.keySet())) {
            delegate.putAll(map);
        }
    }

    @Override
    public void putAll(@NotNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        try (OpContext ignored = listener.reportKeys(Stat.DB_SET, Streams.stream(entries).map(Map.Entry::getKey).toList())) {
            delegate.putAll(entries);
        }
    }

    @Override
    public void putAll(@NotNull Stream<? extends Map.Entry<? extends K, ? extends V>> entries) {
        putAll(entries.toList());   // have to materialize (can't reuse the stream)
    }

    @Override
    public void putAll(@NotNull K @NotNull [] keys, @NotNull V @NotNull [] values) {
        try (OpContext ignored = listener.reportKeys(Stat.DB_SET, keys)) {
            delegate.putAll(keys, values);
        }
    }

    @Override
    public void putAll(@NotNull Iterable<? extends K> keys, @NotNull Iterable<? extends V> values) {
        try (OpContext ignored = listener.reportKeys(Stat.DB_SET, keys)) {
            delegate.putAll(keys, values);
        }
    }

    @Override
    public @Nullable V replace(@NotNull K key, @NotNull V value) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            return delegate.replace(key, value);
        }
    }

    @Override
    public boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            return delegate.replace(key, oldValue, newValue);
        }
    }

    @Override
    public @Nullable V computeIfAbsent(@NotNull K key, @NotNull Function<? super K, ? extends V> mapping) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            return delegate.computeIfAbsent(key, mapping);
        }
    }

    @Override
    public @Nullable V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remapping) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            return delegate.computeIfPresent(key, remapping);
        }
    }

    @Override
    public @Nullable V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remapping) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            return delegate.compute(key, remapping);
        }
    }

    @Override
    public @Nullable V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remapping) {
        try (OpContext ignored = listener.reportKey(Stat.DB_SET, key)) {
            return delegate.merge(key, value, remapping);
        }
    }

    @Override
    public void delete(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Stat.DB_DELETE, key)) {
            delegate.delete(key);
        }
    }

    @Override
    public @Nullable V remove(@NotNull K key) {
        try (OpContext ignored = listener.reportKey(Stat.DB_DELETE, key)) {
            return delegate.remove(key);
        }
    }

    @Override
    public void removeAll(@NotNull K @NotNull [] keys) {
        try (OpContext ignored = listener.reportKeys(Stat.DB_DELETE, keys)) {
            delegate.removeAll(keys);
        }
    }

    @Override
    public void removeAll(@NotNull Iterable<K> keys) {
        try (OpContext ignored = listener.reportKeys(Stat.DB_DELETE, keys)) {
            delegate.removeAll(keys);
        }
    }

    @Override
    public void clear() {
        try (OpContext ignored = listener.report(Stat.DB_DELETE)) {
            delegate.clear();
        }
    }

    @Override
    public void flush() {
        try (OpContext ignored = listener.report(Stat.DB_IO)) {
            delegate.flush();
        }
    }

    @Override
    public void forceFlush() {
        try (OpContext ignored = listener.report(Stat.DB_IO)) {
            delegate.forceFlush();
        }
    }

    @Override
    public void close() {
        try (OpContext ignored = listener.report(Stat.DB_IO)) {
            delegate.close();
        }
    }
}
