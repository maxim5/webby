package io.webby.db.count.primitive;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntIntMap;
import io.webby.db.cache.Persistable;
import org.jetbrains.annotations.NotNull;

public interface IntCounter extends Persistable {
    default int increment(int key) {
        return update(key, 1);
    }

    default int decrement(int key) {
        return update(key, -1);
    }

    int update(int key, int delta);

    int estimateCount(int key);

    @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys);

    default @NotNull IntIntMap estimateCounts(int @NotNull [] keys) {
        return estimateCounts(IntArrayList.from(keys));
    }
}
