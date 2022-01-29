package io.webby.db.count.impl;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import io.webby.db.count.IntSetCounter;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockBasedIntSetCounter implements IntSetCounter {
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final IntSetStorage store;
    private final IntObjectHashMap<IntHashSet> cache;
    private final IntIntHashMap counters;

    public LockBasedIntSetCounter(@NotNull IntSetStorage store) {
        this.store = store;
        this.cache = new IntObjectHashMap<>();  // load anything at the start?
        this.counters = loadFreshCountsSlow(store);
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

    @Override
    public int itemValue(int key, int item) {
        assert item > 0 : "Item unsupported: " + item;
        LOCK.writeLock().lock();
        try {
            IntHashSet items = getOrLoadForKey(key);
            return items.contains(item) ? 1 : items.contains(-item) ? -1 : 0;
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @Override
    public @NotNull IntIntMap itemValues(@NotNull IntContainer keys, int item) {
        assert item > 0 : "Item unsupported: " + item;
        LOCK.writeLock().lock();
        try {
            IntIntHashMap result = new IntIntHashMap(keys.size());
            for (IntCursor cursor : keys) {
                IntHashSet items = getOrLoadForKey(cursor.value);
                result.put(cursor.value, items.contains(item) ? 1 : items.contains(-item) ? -1 : 0);
            }
            return result;
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @Override
    public int estimateCount(int key) {
        LOCK.readLock().lock();
        try {
            return counters.get(key);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @Override
    public @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys) {
        LOCK.readLock().lock();
        try {
            return EasyHppc.slice(counters, keys);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @Override
    public @NotNull IntIntMap estimateAllCounts() {
        LOCK.readLock().lock();
        try {
            return new IntIntHashMap(counters);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @Override
    public void forceFlush() {
        LOCK.writeLock().lock();
        try {
            store.storeBatch(cache, null);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public void close() {
        forceFlush();
    }

    private int update(int key, int item, int delta) {
        LOCK.writeLock().lock();
        try {
            IntHashSet items = getOrLoadForKey(key);
            if (!counters.containsKey(key)) {
                counters.put(key, countItems(items));
            }
            if (items.remove(-item) || items.add(item)) {
                return counters.addTo(key, delta);
            }
            return counters.get(key);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    private @NotNull IntHashSet getOrLoadForKey(int key) {
        IntHashSet events = cache.get(key);
        if (events == null) {
            events = store.load(key);
            cache.put(key, events);
        }
        return events;
    }

    private static @NotNull IntIntHashMap loadFreshCountsSlow(@NotNull IntSetStorage store) {
        IntIntHashMap result = new IntIntHashMap();
        store.loadAll((key, items) -> result.put(key, countItems(items)));
        return result;
    }

    private static int countItems(@NotNull IntHashSet items) {
        int result = 0;
        for (IntCursor cursor : items) {
            if (cursor.value > 0) {
                result++;
            } else {
                result--;
            }
        }
        return result;
    }
}
