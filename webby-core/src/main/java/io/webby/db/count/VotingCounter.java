package io.webby.db.count;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntIntMap;
import io.webby.db.event.Persistable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface VotingCounter extends Persistable {
    int increment(int key, int actor);

    int decrement(int key, int actor);

    int getVote(int key, int actor);

    @NotNull IntIntMap getVotes(@NotNull IntContainer keys, int actor);

    default @NotNull IntIntMap getVotes(int @NotNull [] keys, int actor) {
        return getVotes(IntArrayList.from(keys), actor);
    }

    int estimateCount(int key);

    @NotNull IntIntMap estimateCounts(@NotNull IntContainer keys);

    default @NotNull IntIntMap estimateCounts(int @NotNull [] keys) {
        return estimateCounts(IntArrayList.from(keys));
    }

    @NotNull IntIntMap estimateAllCounts();
}
