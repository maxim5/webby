package io.webby.db.count.impl;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import io.webby.db.count.VotingCounter;
import org.jctools.counters.Counter;
import org.jctools.counters.CountersFactory;
import org.jctools.maps.NonBlockingHashMapLong;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicReference;

@ThreadSafe
public class NonBlockingVotingCounter implements VotingCounter {
    private final VotingStorage store;
    private final NonBlockingHashMapLong<IntSetValue> cache;

    public NonBlockingVotingCounter(@NotNull VotingStorage store) {
        this.store = store;
        this.cache = new NonBlockingHashMapLong<>();
        // TODO[!]: pre-fill
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

    private int update(int key, int actor, int delta) {
        IntSetValue value = getOrLoadForKey(key);
        value.updateActorValue(actor, delta);
        return value.count();
    }

    @Override
    public int getVote(int key, int actor) {
        assert actor > 0 : "Actor unsupported: " + actor;
        return getOrLoadForKey(key).actorValue(actor);
    }

    @Override
    public @NotNull IntIntMap getVotes(@NotNull IntContainer keys, int actor) {
        assert actor > 0 : "Actor unsupported: " + actor;
        IntIntHashMap map = new IntIntHashMap(keys.size());
        getOrLoadForKeys(keys, (key, value) -> map.put(key, value.actorValue(actor)));
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
        IntObjectHashMap<IntHashSet> curr = new IntObjectHashMap<>();
        IntObjectHashMap<IntHashSet> prev = new IntObjectHashMap<>();
        long[] keys = cache.keySetLong();
        for (long key : keys) {
            IntSetValue value = cache.get(key);
            curr.put((int) key, value.copyActors());
            prev.put((int) key, value.currentDbSnapshot());
        }

        store.storeBatch(curr, prev);

        for (long key : keys) {
            // FIX[minor]: log warning if false
            cache.get(key).updateSnapshot(prev.get((int) key), curr.get((int) key));
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

    private @NotNull IntSetValue getOrLoadForKey(int key) {
        IntSetValue value = cache.get(key);
        if (value == null) {
            IntHashSet actors = store.load(key);
            value = new IntSetValue(actors);
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
            store.loadBatch(keysToLoad, (key, actors) -> {
                IntSetValue value = new IntSetValue(actors);
                cache.put(key, value);
                consumer.apply(key, value);
            });
        }
    }

    private static final class IntSetValue {
        private final AtomicReference<IntHashSet> currentDbSnapshot = new AtomicReference<>();
        private final IntHashSet actors;
        private final Counter counter = CountersFactory.createFixedSizeStripedCounter(4);

        public IntSetValue(@NotNull IntHashSet actors) {
            this.actors = actors;
            this.currentDbSnapshot.set(new IntHashSet(actors));
        }

        public int count() {
            return (int) counter.get();
        }

        public synchronized @NotNull IntHashSet copyActors() {
            return new IntHashSet(actors);
        }

        public @NotNull IntHashSet currentDbSnapshot() {
            return currentDbSnapshot.get();
        }

        public boolean updateSnapshot(@NotNull IntHashSet expected, @NotNull IntHashSet snapshot) {
            return currentDbSnapshot.compareAndSet(expected, snapshot);
        }

        public synchronized int actorValue(int actor) {
            return actors.contains(actor) ? 1 : actors.contains(-actor) ? -1 : 0;
        }

        public synchronized void updateActorValue(int actor, int delta) {
            if (actors.remove(-actor) || actors.add(actor)) {
                counter.inc(delta);
            }
        }
    }
}
