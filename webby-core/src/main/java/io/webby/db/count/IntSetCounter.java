package io.webby.db.count;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntIntMap;
import io.webby.db.event.Persistable;
import org.jetbrains.annotations.NotNull;

public interface IntSetCounter extends Persistable {
    int increment(int key, int item);

    int decrement(int key, int item);

    int itemValue(int key, int item);

    @NotNull IntIntMap itemValues(@NotNull IntContainer keys, int item);

    default @NotNull IntIntMap itemValues(int @NotNull [] keys, int item) {
        return itemValues(IntArrayList.from(keys), item);
    }

    int estimateCount(int key);

    @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys);

    default @NotNull IntIntMap estimateCounts(int @NotNull [] keys) {
        return estimateCounts(IntArrayList.from(keys));
    }

    @NotNull IntIntMap estimateAllCounts();
}
