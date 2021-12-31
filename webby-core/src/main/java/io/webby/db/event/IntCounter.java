package io.webby.db.event;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import io.webby.db.kv.KeyValueDb;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IntCounter implements Persistable {
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final IntIntHashMap cache;
    private final KeyValueDb<Integer, Integer> db;

    public IntCounter(@NotNull KeyValueDb<Integer, Integer> db) {
        this.db = db;
        cache = new IntIntHashMap(Math.max(db.size(), 1024));
        db.forEach(cache::put);
    }

    public int increment(int key) {
        return update(key, 1);
    }

    public int decrement(int key) {
        return update(key, -1);
    }

    public int update(int key, int delta) {
        LOCK.writeLock().lock();
        try {
            return cache.addTo(key, delta);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    public int estimateCount(int key) {
        LOCK.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public @NotNull IntIntMap estimateCounts(int @NotNull [] keys) {
        LOCK.readLock().lock();
        try {
            return EasyHppc.slice(cache, keys);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys) {
        LOCK.readLock().lock();
        try {
            return EasyHppc.slice(cache, keys);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public @NotNull IntIntMap estimateAllCounts() {
        LOCK.readLock().lock();
        try {
            return new IntIntHashMap(cache);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @Override
    public void forceFlush() {
        LOCK.writeLock().lock();
        try {
            db.putAll(EasyHppc.toJavaMap(cache));
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @Override
    public void clearCache() {
        forceFlush();
    }

    @Override
    public void close() {
        forceFlush();
    }
}
