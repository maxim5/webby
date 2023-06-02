package io.webby.db.count.primitive;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntIntMap;
import io.webby.db.managed.ManagedPersistent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents an anonymous managed persistent <code>int -> int</code> counting API, or
 * simply put, an efficient thread-safe <code>Map&lt;Integer, Integer&gt;</code>.
 * <p>
 * The counts are computed and stored per integer keys. Each key is independent:
 * <code>key1</code> update doesn't affect any other <code>key2</code>.
 * Depending on the implementation, the API may be blocking or non-blocking
 * globally across the whole map or on a key level.
 * <p>
 * <i>Anonymous</i> counting means the API doesn't distinguish the actors performing modifications.
 * The same actor may update the same key multiple times, and the current per-actor count is not stored.
 * <p>
 * Due to performance considerations, the implementations can but are not required to
 * return exact count values at any moment, just best-effort estimates.
 * These estimates are expected to be eventually consistent with the true values.
 * <p>
 * Possible use cases:
 * <ul>
 *     <li>Page/video view counter</li>
 *     <li>Clicks counter</li>
 * </ul>
 *
 * @see ManagedPersistent
 * @see io.webby.db.count.vote.VotingCounter
 */
@ThreadSafe
public interface IntCounter extends ManagedPersistent {
    /**
     * Increments the value per specified {@code key} by 1 and returns the current total count estimate.
     */
    default int increment(int key) {
        return update(key, 1);
    }

    /**
     * Decrements the value per specified {@code key} by 1 and returns the current total count estimate.
     */
    default int decrement(int key) {
        return update(key, -1);
    }

    /**
     * Updates the value per specified {@code key} by {@code delta} and returns the current total count estimate.
     * The {@code delta} can positive, zero or negative.
     */
    int update(int key, int delta);

    /**
     * Returns the total count estimate per {@code key}.
     */
    int estimateCount(int key);

    /**
     * Returns the total counts for a collection of {@code keys} in a batch.
     * In case of a concurrent modification, the result view map may or may not be read-consistent.
     */
    @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys);

    /**
     * Returns the total counts for an array of {@code keys} in a batch.
     * In case of a concurrent modification, the result view map may or may not be read-consistent.
     */
    default @NotNull IntIntMap estimateCounts(int @NotNull [] keys) {
        return estimateCounts(IntArrayList.from(keys));
    }
}
