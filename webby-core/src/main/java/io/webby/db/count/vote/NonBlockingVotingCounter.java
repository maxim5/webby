package io.webby.db.count.vote;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import io.webby.db.count.StoreChangedEvent;
import org.jctools.counters.Counter;
import org.jctools.counters.CountersFactory;
import org.jctools.maps.NonBlockingHashMapLong;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.IntStream;

@ThreadSafe
public class NonBlockingVotingCounter implements VotingCounter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private final VotingStorage store;
    private final NonBlockingHashMapLong<VoteSet> cache;
    private final AtomicBoolean storeDirty = new AtomicBoolean();

    public NonBlockingVotingCounter(@NotNull VotingStorage store, @NotNull EventBus eventBus) {
        this.store = store;
        this.cache = new NonBlockingHashMapLong<>();  // FIX[minor]: load anything at the start?
        eventBus.register(this);
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

    private int update(int key, int actor, int delta) {
        VoteSet value = getOrLoadVotesForKey(key);
        value.updateVote(actor, delta);
        return value.count();
    }

    @Override
    public int getVote(int key, int actor) {
        assert actor > 0 : "Actor unsupported: " + actor;
        return getOrLoadVotesForKey(key).vote(actor);
    }

    @Override
    public @NotNull IntIntMap getVotes(@NotNull IntContainer keys, int actor) {
        assert actor > 0 : "Actor unsupported: " + actor;
        IntIntHashMap map = new IntIntHashMap(keys.size());
        getOrLoadForKeys(keys, (key, value) -> map.put(key, value.vote(actor)));
        return map;
    }

    @Override
    public int estimateCount(int key) {
        return getOrLoadVotesForKey(key).count();
    }

    @Override
    public @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys) {
        IntIntHashMap map = new IntIntHashMap();
        getOrLoadForKeys(keys, (key, value) -> map.put(key, value.count()));
        return map;
    }

    @Override
    public void forceFlush() {
        IntObjectHashMap<IntHashSet> curr = new IntObjectHashMap<>();
        IntObjectHashMap<IntHashSet> prev = new IntObjectHashMap<>();
        long[] keys = cache.keySetLong();
        for (long key : keys) {
            VoteSet value = cache.get(key);
            curr.put((int) key, value.copyVotes());
            prev.put((int) key, value.currentDbSnapshot());
        }

        store.storeBatch(curr, storeDirty.getAndSet(false) ? null : prev);

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

    // FIX[minor]: expose cache interface
    public @NotNull IntIntMap cache() {
        IntIntHashMap map = new IntIntHashMap();
        for (long key : cache.keySetLong()) {
            map.put((int) key, estimateCount((int) key));
        }
        return map;
    }

    private @NotNull VoteSet getOrLoadVotesForKey(int key) {
        VoteSet value = cache.get(key);
        if (value == null) {
            IntHashSet votes = store.load(key);
            value = new VoteSet(votes);
            cache.put(key, value);
        }
        return value;
    }

    private void getOrLoadForKeys(@NotNull IntContainer keys, @NotNull IntObjectProcedure<VoteSet> consumer) {
        IntArrayList keysToLoad = new IntArrayList();
        for (IntCursor cursor : keys) {
            VoteSet value = cache.get(cursor.value);
            if (value != null) {
                consumer.apply(cursor.value, value);
            } else {
                keysToLoad.add(cursor.value);
            }
        }
        if (!keysToLoad.isEmpty()) {
            store.loadBatch(keysToLoad, (key, intSet) -> {
                VoteSet votes = new VoteSet(intSet);
                cache.put(key, votes);
                consumer.apply(key, votes);
            });
        }
    }

    private static final class VoteSet {
        private final AtomicReference<IntHashSet> currentDbSnapshot = new AtomicReference<>();
        private final IntHashSet votes;
        private final Counter counter = CountersFactory.createFixedSizeStripedCounter(4);

        public VoteSet(@NotNull IntHashSet votes) {
            this.votes = votes;
            this.currentDbSnapshot.set(new IntHashSet(votes));
            this.counter.inc(votes.isEmpty() ? 0 : IntStream.of(votes.keys).map(vote -> Integer.compare(vote, 0)).sum());
        }

        public int count() {
            return (int) counter.get();
        }

        public synchronized @NotNull IntHashSet copyVotes() {
            return new IntHashSet(votes);
        }

        public @NotNull IntHashSet currentDbSnapshot() {
            return currentDbSnapshot.get();
        }

        public boolean updateSnapshot(@NotNull IntHashSet expected, @NotNull IntHashSet snapshot) {
            return currentDbSnapshot.compareAndSet(expected, snapshot);
        }

        public synchronized int vote(int actor) {
            return votes.contains(actor) ? 1 : votes.contains(-actor) ? -1 : 0;
        }

        public synchronized void updateVote(int actor, int delta) {
            if (votes.remove(-actor) || votes.add(actor)) {
                counter.inc(delta);
            }
        }
    }
}
