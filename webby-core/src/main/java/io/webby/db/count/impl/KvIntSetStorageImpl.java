package io.webby.db.count.impl;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import io.webby.db.kv.KeyValueDb;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

import static io.webby.db.count.impl.RobustnessCheck.ensureStorageConsistency;

public class KvIntSetStorageImpl implements IntSetStorage {
    private final KeyValueDb<Integer, IntHashSet> db;

    public KvIntSetStorageImpl(@NotNull KeyValueDb<Integer, IntHashSet> db) {
        this.db = db;
    }

    @Override
    public @NotNull IntHashSet load(int key) {
        return db.getOrDefault(key, new IntHashSet());
    }

    @Override
    public void loadBatch(@NotNull IntContainer keys, @NotNull IntObjectProcedure<@NotNull IntHashSet> consumer) {
        List<IntHashSet> values = db.getAll(EasyHppc.toJavaList(keys));
        assert keys.size() == values.size() :
            "Internal error: keys/values mismatch: keys=%s values=%s".formatted(keys, values);
        Iterator<IntCursor> keyIt = keys.iterator();
        Iterator<IntHashSet> valIt = values.iterator();
        while (keyIt.hasNext()) {
            IntCursor key = keyIt.next();
            IntHashSet value = valIt.next();
            if (value != null) {
                consumer.apply(key.value, value);
            }
        }
    }

    @Override
    public void loadAll(@NotNull IntObjectProcedure<@NotNull IntHashSet> consumer) {
        db.forEach(consumer::apply);
    }

    @Override
    public void storeBatch(@NotNull IntObjectMap<IntHashSet> map, @Nullable IntObjectMap<IntHashSet> prev) {
        assert prev == null || ensureStorageConsistency(this, prev) : "This is Impossible?!";
        db.putAll(EasyHppc.toJavaMap(map));
    }
}
