package io.webby.db.count.impl;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*package*/ interface IntSetStorage {
    @NotNull IntHashSet load(int key);

    void loadBatch(@NotNull IntContainer keys, @NotNull IntObjectProcedure<@NotNull IntHashSet> consumer);

    default @NotNull IntObjectMap<IntHashSet> loadBatch(@NotNull IntContainer keys) {
        IntObjectHashMap<IntHashSet> result = new IntObjectHashMap<>();
        loadBatch(keys, result::put);
        return result;
    }

    void loadAll(@NotNull IntObjectProcedure<@NotNull IntHashSet> consumer);

    default @NotNull IntObjectMap<IntHashSet> loadAll() {
        IntObjectHashMap<IntHashSet> result = new IntObjectHashMap<>();
        loadAll(result::put);
        return result;
    }

    void storeBatch(@NotNull IntObjectMap<IntHashSet> curr, @Nullable IntObjectMap<IntHashSet> prev);
}
