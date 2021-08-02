package io.webby.db.kv.javamap;

import io.webby.db.kv.BaseKeyValueFactory;
import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JavaMapKeyValueFactory extends BaseKeyValueFactory {
    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, JavaMapKeyValueDb::new);
    }

    @Override
    public void close() throws IOException {}

    private static class JavaMapKeyValueDb<K, V> implements KeyValueDb<K, V> {
        private final Map<K, V> map = new HashMap<>();

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public @Nullable V get(@NotNull K key) {
            return map.get(key);
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
        public void flush() {}

        @Override
        public void close() {}
    }
}
