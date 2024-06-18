package io.spbx.webby.db.event;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.mu.util.stream.BiStream;
import io.spbx.util.collect.ListBuilder;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.managed.FlushMode;
import io.spbx.webby.db.managed.HasCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CachingKvdbEventStore<K, E> implements KeyEventStore<K, E>, HasCache<Multimap<K, E>> {
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();  // Alternative: Guava Striped

    private final KeyValueDb<K, List<E>> db;
    private final Multimap<K, E> cache = ArrayListMultimap.create();
    private final KeyEventStoreFactory.Compacter<E> compacter;

    private final int cacheSizeSoftLimit;
    private final int cacheSizeHardLimit;
    private final int flushBatchSize;

    public CachingKvdbEventStore(@NotNull KeyValueDb<K, List<E>> db,
                                 @NotNull KeyEventStoreFactory.Compacter<E> compacter,
                                 int cacheSizeSoftLimit,
                                 int cacheSizeHardLimit,
                                 int flushBatchSize) {
        assert cacheSizeSoftLimit > 0 :
            "Invalid cache size limits: soft=%d hard=%d".formatted(cacheSizeSoftLimit, cacheSizeHardLimit);
        assert cacheSizeHardLimit >= cacheSizeSoftLimit :
            "Invalid cache size limits: soft=%d hard=%d".formatted(cacheSizeSoftLimit, cacheSizeHardLimit);
        this.db = db;
        this.compacter = compacter;
        this.cacheSizeSoftLimit = cacheSizeSoftLimit;
        this.cacheSizeHardLimit = cacheSizeHardLimit;
        this.flushBatchSize = flushBatchSize;
    }

    @Override
    public void append(@NotNull K key, @NotNull E event) {
        LOCK.readLock().lock();
        try {
            cache.put(key, event);
        } finally {
            LOCK.readLock().unlock();
        }
        if (cache.size() >= cacheSizeHardLimit) {
            flush(FlushMode.FULL_COMPACT);
        }
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
        if (cache.size() >= cacheSizeSoftLimit) {
            flush(FlushMode.INCREMENTAL);
        }
    }

    @Override
    public void flush(@NotNull FlushMode mode) {
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
                    .mapKeys(compacter::compactInMemory)
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
            flush(FlushMode.FULL_CLEAR);
            db.close();
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    public @NotNull Multimap<K, E> cache() {
        return cache;
    }

    @VisibleForTesting
    @NotNull KeyValueDb<K, List<E>> db() {
        return db;
    }

    private static <E> @NotNull List<E> concatToList(@NotNull Collection<E> cached, @Nullable List<E> persistent) {
        if (persistent != null) {
            return ListBuilder.concat(cached, persistent);
        }
        return new ArrayList<>(cached);
    }
}
