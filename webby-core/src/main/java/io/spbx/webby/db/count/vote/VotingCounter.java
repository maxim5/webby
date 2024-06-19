package io.spbx.webby.db.count.vote;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntIntMap;
import io.spbx.webby.db.managed.ManagedPersistent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents an identified managed persistent <code>(int, int) -> int</code> counting API, or
 * simply put, an efficient thread-safe <code>Map&lt;Integer, Map&lt;Integer, Integer&gt;&gt;</code>.
 * <p>
 * The counts are computed and stored per integer keys. Each key is independent:
 * <code>key1</code> update doesn't affect any other <code>key2</code>.
 * Depending on the implementation, the API may be blocking or non-blocking
 * globally across the whole map or on a key level.
 * <p>
 * Each vote (i.e. actor modification) can only be +1 or -1. Zero means no vote.
 * <p>
 * <i>Identified</i> counting means the API distinguishes the actors performing modifications.
 * The same actor may not be allows to update the same key multiple times, since the current per-actor count is stored.
 * <p>
 * Due to performance considerations, the implementations can but are not required to
 * return exact count values at any moment, just best-effort estimates.
 * These estimates are expected to be eventually consistent with the true values.
 * <p>
 * Possible use cases:
 * <ul>
 *     <li>Likes counter</li>
 * </ul>
 *
 * @see ManagedPersistent
 * @see io.spbx.webby.db.count.primitive.IntCounter
 */
@ThreadSafe
public interface VotingCounter extends ManagedPersistent {
    /**
     * Increments the value per specified {@code key} by 1 for the specified {@code actor} and
     * returns the current total count estimate.
     */
    int increment(int key, int actor);

    /**
     * Decrements the value per specified {@code key} by 1 for the specified {@code actor} and
     * returns the current total count estimate.
     */
    int decrement(int key, int actor);

    /**
     * Returns the current {@code actor} vote for the {@code key}. Can be one of: -1, 0, 1.
     */
    int getVote(int key, int actor);

    /**
     * Returns the current {@code actor} votes for the {@code keys} container.
     * For each key result can be one of: -1, 0, 1.
     * In case of a concurrent modification, the result view map may or may not be read-consistent.
     */
    @NotNull IntIntMap getVotes(@NotNull IntContainer keys, int actor);

    /**
     * Returns the current {@code actor} votes for the {@code keys} array.
     * For each key result can be one of: -1, 0, 1.
     * In case of a concurrent modification, the result view map may or may not be read-consistent.
     */
    default @NotNull IntIntMap getVotes(int @NotNull [] keys, int actor) {
        return getVotes(IntArrayList.from(keys), actor);
    }

    /**
     * Returns the current count estimate per {@code key}.
     */
    int estimateCount(int key);

    /**
     * Returns the total counts for a collection of {@code keys} for all actors in a batch.
     * In case of a concurrent modification, the result view map may or may not be read-consistent.
     */
    @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys);

    /**
     * Returns the total counts for an array of {@code keys} for all actors in a batch.
     * In case of a concurrent modification, the result view map may or may not be read-consistent.
     */
    default @NotNull IntIntMap estimateCounts(int @NotNull [] keys) {
        return estimateCounts(IntArrayList.from(keys));
    }
}
