package io.spbx.webby.db.count.vote;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import io.spbx.util.hppc.EasyHppc;
import io.spbx.webby.db.DbReadyEvent;
import io.spbx.webby.db.count.StoreChangedEvent;
import io.spbx.webby.db.managed.FlushMode;
import io.spbx.webby.db.managed.HasCache;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.stream.IntStream;

@ThreadSafe
public class LockBasedVotingCounter implements VotingCounter, HasCache<IntIntMap> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final VotingStorage store;
    private final IntObjectHashMap<IntHashSet> cache;
    private final IntIntHashMap counters;
    private final AtomicBoolean storeDirty = new AtomicBoolean();

    public LockBasedVotingCounter(@NotNull VotingStorage store, @NotNull EventBus eventBus) {
        this.store = store;
        this.cache = new IntObjectHashMap<>();  // FIX[minor]: load anything at the start?
        this.counters = new IntIntHashMap(1024);
        eventBus.register(this);
    }

    @Subscribe
    public void dbReady(@NotNull DbReadyEvent event) {
        store.loadAll((key, votes) -> counters.put(key, countVotes(votes)));
    }

    @Subscribe
    public void storeChanged(@NotNull StoreChangedEvent event) {
        if (store.storeId().equals(event.storeId())) {
            log.at(Level.INFO).log("External change detected for %s. Marking the store dirty", event.storeId());
            storeDirty.set(true);
        }
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
    public void flush(@NotNull FlushMode mode) {
        LOCK.readLock().lock();
        try {
            store.storeBatch(cache, null);
        } finally {
            LOCK.readLock().unlock();
        }

        if (storeDirty.getAndSet(false) || mode.isFlushAll()) {
            LOCK.writeLock().lock();
            try {
                counters.clear();
                store.loadAll((key, votes) -> counters.put(key, countVotes(votes)));
            } finally {
                LOCK.writeLock().unlock();
            }
        }
    }

    @Override
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
                counters.put(key, countVotes(actors));
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

    private static int countVotes(@NotNull IntHashSet votes) {
        return votes.isEmpty() ? 0 : IntStream.of(votes.keys).map(vote -> Integer.compare(vote, 0)).sum();
    }
}
