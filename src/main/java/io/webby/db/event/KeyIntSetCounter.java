package io.webby.db.event;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KeyIntSetCounter implements Persistable {
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final IntObjectHashMap<IntHashSet> cache = new IntObjectHashMap<>();
    private final IntIntHashMap counters = new IntIntHashMap();
    private final KeyValueDb<Integer, IntHashSet> db;

    public KeyIntSetCounter(@NotNull KeyValueDb<Integer, IntHashSet> db) {
        this.db = db;
    }

    public int update(int key, int eventId, int delta) {
        LOCK.writeLock().lock();
        try {
            IntHashSet events = cache.get(key);
            if (events == null) {
                events = db.getOrDefault(key, new IntHashSet());
                cache.put(key, events);
            }
            if (events.add(eventId)) {
                return counters.addTo(key, delta);
            }
            return counters.get(key);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    public int increment(int key, int eventId) {
        return update(key, eventId, 1);
    }

    public int decrement(int key, int eventId) {
        return update(key, eventId, -1);
    }

    public int estimateCount(int key) {
        LOCK.readLock().lock();
        try {
            return counters.get(key);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public @NotNull IntIntMap estimateCounts(int @NotNull [] keys) {
        LOCK.readLock().lock();
        try {
            IntIntHashMap result = new IntIntHashMap(keys.length);
            for (int key : keys) {
                result.put(key, counters.get(key));
            }
            return result;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public void forceFlush() {
        LOCK.writeLock().lock();
        try {
            Map<Integer, IntHashSet> map = new HashMap<>(cache.size());
            cache.forEach((IntObjectProcedure<? super IntHashSet>) map::put);
            db.putAll(map);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    public void clearCache() {
        cache.clear();
    }

    public void close() {
        forceFlush();
    }
}
