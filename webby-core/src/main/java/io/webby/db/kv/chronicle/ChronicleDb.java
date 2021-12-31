package io.webby.db.kv.chronicle;

import io.webby.db.kv.impl.DefaultKeyValueDb;
import net.openhft.chronicle.map.ChronicleMap;
import org.jetbrains.annotations.NotNull;

public class ChronicleDb<K, V> extends DefaultKeyValueDb<K, V, ChronicleMap<K, V>> {
    public ChronicleDb(@NotNull ChronicleMap<K, V> map) {
        super(map);
    }

    @Override
    public long longSize() {
        return map.longSize();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        map.close();
    }
}
