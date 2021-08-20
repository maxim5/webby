package io.webby.db.kv.mapdb;

import io.webby.db.kv.DefaultKeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DB;
import org.mapdb.HTreeMap;

public class MapDbImpl<K, V> extends DefaultKeyValueDb<K, V, HTreeMap<K, V>> {
    private final DB db;

    public MapDbImpl(@NotNull DB db, @NotNull HTreeMap<K, V> map) {
        super(map);
        this.db = db;
    }

    @Override
    public long longSize() {
        return map.sizeLong();
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
