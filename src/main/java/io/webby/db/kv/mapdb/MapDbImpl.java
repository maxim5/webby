package io.webby.db.kv.mapdb;

import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.DB;
import org.mapdb.HTreeMap;

public class MapDbImpl<K, V> implements KeyValueDb<K, V> {
    private final DB db;
    private final HTreeMap<K, V> map;

    public MapDbImpl(@NotNull DB db, @NotNull HTreeMap<K, V> map) {
        this.db = db;
        this.map = map;
    }

    /*@NotNull
    public static <K, V> MapDbImpl<K, V> persistentHashMap(@NotNull String path,
                                                           @NotNull String name,
                                                           @NotNull Serializer<K> keySerializer,
                                                           @NotNull Serializer<V> valueSerializer) {
        DB db = DBMaker.fileDB(path).make();
        HTreeMap<K, V> map = db.hashMap(name, keySerializer, valueSerializer).createOrOpen();
        return new MapDbImpl<>(db, map);
    }

    @NotNull
    public static <K, V> MapDbImpl<K, V> inMemoryHashMap(@NotNull String name,
                                                         @NotNull Serializer<K> keySerializer,
                                                         @NotNull Serializer<V> valueSerializer) {
        DB db = DBMaker.memoryDB().make();
        HTreeMap<K, V> map = db.hashMap(name, keySerializer, valueSerializer).createOrOpen();
        return new MapDbImpl<>(db, map);
    }*/

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        map.put(key, value);
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return map.get(key);
    }

    @Override
    public @Nullable V put(@NotNull K key, @NotNull V value) {
        return map.put(key, value);
    }

    @Override
    public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
        return map.putIfAbsent(key, value);
    }

    @Override
    public @NotNull V getOrDefault(@NotNull K key, @NotNull V def) {
        return map.getOrDefault(key, def);
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
