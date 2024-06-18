package io.spbx.webby.db.count.primitive;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.procedures.IntIntProcedure;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the persistent storage backing the {@link IntCounter}.
 */
public interface IntCountStorage {
    default int size() {
        return -1;
    }

    void loadAll(@NotNull IntIntProcedure consumer);

    default @NotNull IntIntMap loadAll() {
        IntIntMap result = new IntIntHashMap();
        loadAll(result::put);
        return result;
    }

    void storeBatch(@NotNull IntIntMap map);
}
