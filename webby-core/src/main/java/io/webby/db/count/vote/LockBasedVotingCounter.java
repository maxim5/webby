package io.webby.db.count.vote;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ThreadSafe
public class LockBasedVotingCounter implements VotingCounter {
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final VotingStorage store;
    private final IntObjectHashMap<IntHashSet> cache;
    private final IntIntHashMap counters;

    public LockBasedVotingCounter(@NotNull VotingStorage store) {
        this.store = store;
        this.cache = new IntObjectHashMap<>();  // FIX[minor]: load anything at the start?
        this.counters = loadFreshCountsSlow(store);
    }

    @Override
    public int increment(int key, int actor) {
        assert actor > 0 : "Actor unsupported: " + actor;
        return update(key, actor, 1);
    }

    @Override
    public int decrement(int key, int actor) {
        assert actor > 0 : "Actor unsupported: " + actor;
        return update(key, -actor, -1);
    }

    @Override
    public int getVote(int key, int actor) {
        assert actor > 0 : "Actor unsupported: " + actor;
        LOCK.writeLock().lock();
        try {
            IntHashSet actors = getOrLoadForKey(key);
            return actors.contains(actor) ? 1 : actors.contains(-actor) ? -1 : 0;
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @Override
    public @NotNull IntIntMap getVotes(@NotNull IntContainer keys, int actor) {
        assert actor > 0 : "Actor unsupported: " + actor;
        LOCK.writeLock().lock();
        try {
            IntIntHashMap result = new IntIntHashMap(keys.size());
            for (IntCursor cursor : keys) {
                IntHashSet actors = getOrLoadForKey(cursor.value);
                result.put(cursor.value, actors.contains(actor) ? 1 : actors.contains(-actor) ? -1 : 0);
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
    public void forceFlush() {
        LOCK.readLock().lock();
        try {
            store.storeBatch(cache, null);
        } finally {
            LOCK.readLock().unlock();
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

    public @NotNull IntIntMap cache() {
        LOCK.readLock().lock();
        try {
            return new IntIntHashMap(counters);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    private int update(int key, int actor, int delta) {
        LOCK.writeLock().lock();
        try {
            IntHashSet actors = getOrLoadForKey(key);
            if (!counters.containsKey(key)) {
                counters.put(key, countActors(actors));
            }
            if (actors.remove(-actor) || actors.add(actor)) {
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

    private static @NotNull IntIntHashMap loadFreshCountsSlow(@NotNull VotingStorage store) {
        IntIntHashMap result = new IntIntHashMap();
        store.loadAll((key, actors) -> result.put(key, countActors(actors)));
        return result;
    }

    private static int countActors(@NotNull IntHashSet actors) {
        int result = 0;
        for (IntCursor cursor : actors) {
            if (cursor.value > 0) {
                result++;
            } else {
                result--;
            }
        }
        return result;
    }
}
