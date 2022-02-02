package io.webby.db.count.vote;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import io.webby.db.StorageType;
import io.webby.db.count.StoreId;
import io.webby.db.kv.KeyValueDb;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.webby.db.count.vote.Consistency.checkStorageConsistency;

public class KvVotingStorage implements VotingStorage {
    private final StoreId storeId;
    private final KeyValueDb<Integer, IntHashSet> db;

    public KvVotingStorage(@NotNull String name, @NotNull KeyValueDb<Integer, IntHashSet> db) {
        this.storeId = new StoreId(StorageType.KEY_VALUE_DB, name);
        this.db = db;
    }

    @Override
    public @NotNull StoreId storeId() {
        return storeId;
    }

    @Override
    public @NotNull IntHashSet load(int key) {
        return db.getOrDefault(key, new IntHashSet());
    }

    @Override
    public void loadBatch(@NotNull IntContainer keys, @NotNull IntObjectProcedure<@NotNull IntHashSet> consumer) {
        // keys.iterator() gives a different order each time
        // See https://github.com/carrotsearch/hppc/issues/14
        // FIX[minor]: is there is a cleaner solution to avoid two iterators? getAllToMap()?
        ArrayList<Integer> keysFixedOrder = EasyHppc.toJavaList(keys);
        List<IntHashSet> values = db.getAll(keysFixedOrder);
        assert keysFixedOrder.size() == values.size() :
            "Internal error: keys/values mismatch: keys=%s values=%s".formatted(keysFixedOrder, values);
        Iterator<Integer> keyIt = keysFixedOrder.iterator();
        Iterator<IntHashSet> valIt = values.iterator();
        while (keyIt.hasNext()) {
            int key = keyIt.next();
            IntHashSet value = valIt.next();
            if (value != null && !value.isEmpty()) {
                consumer.apply(key, value);
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
