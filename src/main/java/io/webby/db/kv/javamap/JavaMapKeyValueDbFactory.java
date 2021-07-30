package io.webby.db.kv.javamap;

import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueDbFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class JavaMapKeyValueDbFactory implements KeyValueDbFactory {
    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return new JavaMapKeyValueDb<>();
    }

    private static class JavaMapKeyValueDb<K, V> implements KeyValueDb<K, V> {
        private final Map<K, V> map = new HashMap<>();

        @Override
        public void set(@NotNull K key, @NotNull V value) {
            map.put(key, value);
        }

        @Override
        public @Nullable V get(@NotNull K key) {
            return map.get(key);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }
}
