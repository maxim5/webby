package io.webby.db.event;

import com.google.common.collect.Multimap;
import com.google.mu.util.stream.BiStream;
import io.webby.db.kv.KeyValueDb;
import io.webby.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CachingKvdbEventStore<K, E> implements KeyEventStore<K, E> {
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();  // Alternative: Guava Striped

    private final Multimap<K, E> cache;
    private final KeyValueDb<K, List<E>> db;

    private final int maxCacheSizeBeforeFlush;
    private final int flushBatchSize;

    public CachingKvdbEventStore(@NotNull Multimap<K, E> cache,
                                 @NotNull KeyValueDb<K, List<E>> db,
                                 int maxCacheSizeBeforeFlush, int flushBatchSize) {
        this.cache = cache;
        this.db = db;
        this.maxCacheSizeBeforeFlush = maxCacheSizeBeforeFlush;
        this.flushBatchSize = flushBatchSize;
    }

    @Override
    public void append(@NotNull K key, @NotNull E event) {
        cache.put(key, event);
    }

    @Override
    public @NotNull List<E> getAll(@NotNull K key) {
        LOCK.readLock().lock();
        try {
            Collection<E> cached = cache.get(key);
            List<E> persistent = db.get(key);
            return concatToList(cached, persistent);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @Override
    public void deleteAll(@NotNull K key) {
        LOCK.writeLock().lock();
        try {
            cache.removeAll(key);
            db.delete(key);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @Override
    public void flush() {
        if (cache.size() >= maxCacheSizeBeforeFlush) {
            forceFlush();
        }
    }

    @Override
    public void forceFlush() {
        LOCK.writeLock().lock();
        try {
            ArrayList<K> allKeys = new ArrayList<>(cache.keySet());
            int totalSize = allKeys.size();
            for (int i = 0; i < totalSize; i += flushBatchSize) {
                int batch = Math.min(flushBatchSize, totalSize - i);
                List<K> keys = allKeys.subList(i, i + batch);
                List<@Nullable List<E>> values = db.getAll(keys);
                assert keys.size() == values.size() :
                        "Internal error: keys/values mismatch: keys=%s values=%s".formatted(keys, values);
                List<List<E>> combined = BiStream.zip(keys, values)
                        .mapKeys(cache::get)
                        .mapToObj(CachingKvdbEventStore::concatToList)
                        .toList();
                db.putAll(keys, combined);
            }
            cache.clear();
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        LOCK.writeLock().lock();
        try {
            forceFlush();
            db.close();
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @VisibleForTesting
    @NotNull Multimap<K, E> cache() {
        return cache;
    }

    private static <E> @NotNull List<E> concatToList(@NotNull Collection<E> cached, @Nullable List<E> persistent) {
        if (persistent != null) {
            return EasyIterables.concat(cached, persistent);
        }
        return new ArrayList<>(cached);
    }
}
