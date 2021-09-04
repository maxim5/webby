package io.webby.db.kv.oak;

import com.yahoo.oak.OakMap;
import io.webby.db.kv.InMemoryDb;
import io.webby.db.kv.impl.DefaultKeyValueDb;
import org.jetbrains.annotations.NotNull;

public class OakDb<K, V> extends DefaultKeyValueDb<K, V, OakMap<K, V>> implements InMemoryDb<K, V> {
    public OakDb(@NotNull OakMap<K, V> map) {
        super(map);
    }

    @Override
    public void clear() {
        // See https://github.com/yahoo/Oak/issues/173
        for (K key : map.keySet()) {
            map.remove(key);
        }
    }

    @Override
    public void close() {
        map.close();
    }
}
