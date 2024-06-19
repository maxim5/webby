package io.spbx.webby.db.kv.javamap;

import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.InMemoryDb;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
import io.spbx.webby.db.kv.impl.DefaultKeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

public class JavaMapDbFactory extends BaseKeyValueFactory {
    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, HashMapDb::new);
    }

    @VisibleForTesting
    public @NotNull <K, V>KeyValueDb<K, V> inMemoryDb() {
        return new HashMapDb<>();
    }

    @VisibleForTesting
    public @NotNull <K, V> KeyValueDb<K, V> inMemoryDb(@NotNull Map<K, V> map) {
        return new HashMapDb<>(map);
    }

    private static class HashMapDb<K, V> extends DefaultKeyValueDb<K, V, Map<K, V>> implements InMemoryDb<K, V> {
        public HashMapDb() {
            super(new HashMap<>());
        }

        public HashMapDb(@NotNull Map<K, V> map) {
            super(map);
        }
    }
}
