package io.webby.db.count.primitive;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.webby.db.DbReadyEvent;
import io.webby.db.cache.FlushMode;
import io.webby.db.cache.HasCache;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An {@link IntCounter} implementation based on a global read-write lock.
 * <p>
 * Always returns exact counts, batch methods always return a consistent view.
 * <p>
 * The implementation uses an in-memory cache and a persistent storage.
 */
@ThreadSafe
public class LockBasedIntCounter implements IntCounter, HasCache<IntIntMap> {
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final IntCountStorage store;
    private final IntIntHashMap cache;

    public LockBasedIntCounter(@NotNull IntCountStorage store, @NotNull EventBus eventBus) {
        this.store = store;
        this.cache = new IntIntHashMap(Math.max(store.size(), 1024));
        eventBus.register(this);
    }

    @Subscribe
    public void dbReady(@NotNull DbReadyEvent event) {
        store.loadAll(cache::put);
    }

    @Override
    public int update(int key, int delta) {
        LOCK.writeLock().lock();
        try {
            return cache.addTo(key, delta);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @Override
    public int estimateCount(int key) {
        LOCK.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @Override
    public @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys) {
        LOCK.readLock().lock();
        try {
            return EasyHppc.slice(cache, keys);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @Override
    public void flush(@NotNull FlushMode mode) {
        LOCK.readLock().lock();
        try {
            store.storeBatch(cache);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @Override
    public @NotNull IntIntMap cache() {
        LOCK.readLock().lock();
        try {
            return new IntIntHashMap(cache);
        } finally {
            LOCK.readLock().unlock();
        }
    }
}
