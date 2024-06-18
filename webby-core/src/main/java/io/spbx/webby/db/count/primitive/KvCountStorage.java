package io.spbx.webby.db.count.primitive;

import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.procedures.IntIntProcedure;
import io.spbx.util.hppc.EasyHppc;
import io.spbx.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;

public class KvCountStorage implements IntCountStorage {
    private final KeyValueDb<Integer, Integer> db;

    public KvCountStorage(@NotNull KeyValueDb<Integer, Integer> db) {
        this.db = db;
    }

    @Override
    public int size() {
        return db.size();
    }

    @Override
    public void loadAll(@NotNull IntIntProcedure consumer) {
        db.forEach(consumer::apply);
    }

    @Override
    public void storeBatch(@NotNull IntIntMap map) {
        db.putAll(EasyHppc.toJavaMap(map));
    }
}
