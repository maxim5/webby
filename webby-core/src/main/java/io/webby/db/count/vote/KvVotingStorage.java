package io.webby.db.count.vote;

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

import static io.webby.db.count.vote.Consistency.checkStorageConsistency;

public class KvVotingStorage implements VotingStorage {
    private final KeyValueDb<Integer, IntHashSet> db;

    public KvVotingStorage(@NotNull KeyValueDb<Integer, IntHashSet> db) {
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
            if (value != null && !value.isEmpty()) {
                consumer.apply(key.value, value);
            }
        }
    }

    @Override
    public void loadAll(@NotNull IntObjectProcedure<@NotNull IntHashSet> consumer) {
        db.forEach((key, value) -> {
            if (!value.isEmpty()) {
                consumer.apply(key, value);
            }
        });
    }

    @Override
    public void storeBatch(@NotNull IntObjectMap<IntHashSet> curr, @Nullable IntObjectMap<IntHashSet> prev) {
        assert prev == null || checkStorageConsistency(this, curr, prev) : "This is Impossible?!";
        db.putAll(EasyHppc.toJavaMap(curr));
    }
}
