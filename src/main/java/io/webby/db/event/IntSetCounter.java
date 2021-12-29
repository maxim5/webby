package io.webby.db.event;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import io.webby.db.kv.KeyValueDb;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IntSetCounter implements Persistable {
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final KeyValueDb<Integer, IntHashSet> db;
    private final IntObjectHashMap<IntHashSet> cache;
    private final IntIntHashMap counters;

    public IntSetCounter(@NotNull KeyValueDb<Integer, IntHashSet> db) {
        this.db = db;
        this.cache = new IntObjectHashMap<>();  // load anything at the start?
        this.counters = loadFreshCountsSlow(db);
    }

    public int increment(int key, int eventId) {
        return update(key, eventId, 1);
    }

    public int decrement(int key, int eventId) {
        return update(key, -eventId, -1);
    }

    public int eventValue(int key, int eventId) {
        LOCK.readLock().lock();
        try {
            IntHashSet events = getOrLoadAllEvents(key);
            return events.contains(eventId) ? 1 : events.contains(-eventId) ? -1 : 0;
        } finally {
            LOCK.readLock().unlock();
        }
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
            return EasyHppc.slice(counters, keys);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys) {
        LOCK.readLock().lock();
        try {
            return EasyHppc.slice(counters, keys);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public @NotNull IntIntMap estimateAllCounts() {
        LOCK.readLock().lock();
        try {
            return new IntIntHashMap(counters);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public void forceFlush() {
        LOCK.writeLock().lock();
        try {
            db.putAll(EasyHppc.toJavaMap(cache));
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

    private int update(int key, int eventId, int delta) {
        assert eventId != 0 : "EventId unsupported: " + eventId;
        LOCK.writeLock().lock();
        try {
            IntHashSet events = getOrLoadAllEvents(key);
            if (!counters.containsKey(key)) {
                counters.put(key, countEvents(events));
            }
            if (events.remove(-eventId) || events.add(eventId)) {
                return counters.addTo(key, delta);
            }
            return counters.get(key);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    private @NotNull IntHashSet getOrLoadAllEvents(int key) {
        IntHashSet events = cache.get(key);
        if (events == null) {
            events = db.getOrDefault(key, new IntHashSet());
            LOCK.writeLock().lock();
            try {
                cache.put(key, events);
            } finally {
                LOCK.writeLock().unlock();
            }
        }
        return events;
    }

    private static @NotNull IntIntHashMap loadFreshCountsSlow(@NotNull KeyValueDb<Integer, IntHashSet> db) {
        IntIntHashMap result = new IntIntHashMap();
        db.forEach((key, events) -> result.put(key, countEvents(events)));
        return result;
    }

    private static int countEvents(@NotNull IntHashSet events) {
        int result = 0;
        for (IntCursor cursor : events) {
            if (cursor.value > 0) {
                result++;
            } else {
                result--;
            }
        }
        return result;
    }
}
