package io.webby.db.kv.javamap;

import io.webby.db.kv.DbOptions;
import io.webby.db.kv.InMemoryDb;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.webby.db.kv.impl.DefaultKeyValueDb;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class JavaMapDbFactory extends BaseKeyValueFactory {
    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, HashMapDb::new);
    }

    private static class HashMapDb<K, V> extends DefaultKeyValueDb<K, V, HashMap<K, V>> implements InMemoryDb<K, V> {
        public HashMapDb() {
            super(new HashMap<>());
        }
    }
}
