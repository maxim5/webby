package io.webby.db.kv.javamap;

import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.webby.db.kv.impl.DefaultKeyValueDb;
import io.webby.db.kv.InMemoryDb;
import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;

public class JavaMapDbFactory extends BaseKeyValueFactory {
    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, HashMapDb::new);
    }

    @Override
    public void close() throws IOException {}

    private static class HashMapDb<K, V> extends DefaultKeyValueDb<K, V, HashMap<K, V>> implements InMemoryDb<K, V> {
        public HashMapDb() {
            super(new HashMap<>());
        }
    }
}
