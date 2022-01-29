package io.webby.db.count.impl;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import io.webby.db.count.IntSetCounter;
import org.jctools.counters.Counter;
import org.jctools.counters.CountersFactory;
import org.jctools.maps.NonBlockingHashMapLong;
import org.jetbrains.annotations.NotNull;

public class NonBlockingIntSetCounter implements IntSetCounter {
    private final IntSetStorage store;
    private final NonBlockingHashMapLong<IntSetValue> cache;

    public NonBlockingIntSetCounter(@NotNull IntSetStorage store) {
        this.store = store;
        this.cache = new NonBlockingHashMapLong<>();
        // TODO[!]: pre-fill
    }

    @Override
    public int increment(int key, int item) {
        assert item > 0 : "Item unsupported: " + item;
        return update(key, item, 1);
    }

    @Override
    public int decrement(int key, int item) {
        assert item > 0 : "Item unsupported: " + item;
        return update(key, -item, -1);
    }

    private int update(int key, int item, int delta) {
        IntSetValue value = getOrLoadForKey(key);
        value.updateItem(item, delta);
        return value.count();
    }

    @Override
    public int itemValue(int key, int item) {
        assert item > 0 : "Item unsupported: " + item;
        return getOrLoadForKey(key).itemValue(item);
    }

    @Override
    public @NotNull IntIntMap itemValues(@NotNull IntContainer keys, int item) {
        assert item > 0 : "Item unsupported: " + item;
        IntIntHashMap map = new IntIntHashMap(keys.size());
        getOrLoadForKeys(keys, (key, value) -> map.put(key, value.itemValue(item)));
        return map;
    }

    @Override
    public int estimateCount(int key) {
        return getOrLoadForKey(key).count();
    }

    @Override
    public @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys) {
        IntIntHashMap map = new IntIntHashMap();
        getOrLoadForKeys(keys, (key, value) -> map.put(key, value.count()));
        return map;
    }

    @Override
    public @NotNull IntIntMap estimateAllCounts() {
        IntIntHashMap map = new IntIntHashMap();
        for (long key : cache.keySetLong()) {
            int intKey = (int) key;
            map.put(intKey, estimateCount(intKey));
        }
        return map;
    }

    @Override
    public void forceFlush() {
        IntObjectHashMap<IntHashSet> map = new IntObjectHashMap<>();
        long[] keys = cache.keySetLong();
        for (long key : keys) {
            IntSetValue value = cache.get(key);
            map.put((int) key, value.copyItems());
        }
        store.storeBatch(map, null);
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public void close() {
        forceFlush();
    }

    private @NotNull IntSetValue getOrLoadForKey(int key) {
        IntSetValue value = cache.get(key);
        if (value == null) {
            IntHashSet items = store.load(key);
            value = new IntSetValue(items);
            cache.put(key, value);
        }
        return value;
    }

    private void getOrLoadForKeys(@NotNull IntContainer keys, @NotNull IntObjectProcedure<IntSetValue> consumer) {
        IntHashSet keysToLoad = new IntHashSet();
        for (IntCursor cursor : keys) {
            IntSetValue value = cache.get(cursor.value);
            if (value != null) {
                consumer.apply(cursor.value, value);
            } else {
                keysToLoad.add(cursor.value);
            }
        }
        if (!keysToLoad.isEmpty()) {
            store.loadBatch(keysToLoad, (key, items) -> {
                IntSetValue value = new IntSetValue(items);
                cache.put(key, value);
                consumer.apply(key, value);
            });
        }
    }

    private static final class IntSetValue {
        // TODO[!]: store original items
        private final IntHashSet items;
        private final Counter counter = CountersFactory.createFixedSizeStripedCounter(4);

        public IntSetValue(@NotNull IntHashSet items) {
            this.items = items;
        }

        public int count() {
            return (int) counter.get();
        }

        public synchronized @NotNull IntHashSet copyItems() {
            return new IntHashSet(items);
        }

        public synchronized int itemValue(int item) {
            return items.contains(item) ? 1 : items.contains(-item) ? -1 : 0;
        }

        public synchronized void updateItem(int item, int delta) {
            if (items.remove(-item) || items.add(item)) {
                counter.inc(delta);
            }
        }
    }
}
