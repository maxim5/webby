package io.webby.db.count.vote;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.google.common.flogger.FluentLogger;
import io.webby.util.base.EasyObjects;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/*package*/ class Consistency {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final int MAX_ERRORS = 10;

    public static boolean checkStorageConsistency(@NotNull VotingStorage storage,
                                                  @NotNull IntObjectMap<IntHashSet> curr,
                                                  @NotNull IntObjectMap<IntHashSet> prev) {
        IntObjectMap<IntHashSet> state = buildState(curr, prev);
        List<Mismatch> mismatches = collectMismatches(storage, state);

        if (!mismatches.isEmpty()) {
            log.at(Level.WARNING).log(
                "Storage %s state changed for %d keys and will be overwritten from cache", storage, mismatches.size());
            int i = 0;
            for (Mismatch mismatch : mismatches) {
                IntHashSet addedInDb = EasyHppc.subtract(mismatch.actual(), mismatch.expected());
                IntHashSet removedInDb = EasyHppc.subtract(mismatch.expected(), mismatch.actual());
                log.at(Level.WARNING).log("key=%d added=%s removed=%s", mismatch.key, addedInDb, removedInDb);
                if (++i >= MAX_ERRORS) {
                    break;
                }
            }
        }

        return true;
    }

    private static @NotNull IntObjectMap<IntHashSet> buildState(@NotNull IntObjectMap<IntHashSet> curr,
                                                                @NotNull IntObjectMap<IntHashSet> prev) {
        IntHashSet addedKeys = EasyHppc.subtract(curr.keys(), prev.keys());
        if (addedKeys.isEmpty()) {
            return prev;
        }
        IntObjectHashMap<IntHashSet> state = new IntObjectHashMap<>(prev);
        for (IntCursor cursor : addedKeys) {
            state.put(cursor.value, new IntHashSet());
        }
        return state;
    }

    private static @NotNull List<Mismatch> collectMismatches(@NotNull VotingStorage storage,
                                                             @NotNull IntObjectMap<IntHashSet> expectedState) {
        IntObjectMap<IntHashSet> actual = storage.loadBatch(expectedState.keys());
        if (actual.equals(expectedState)) {
            return List.of();
        }
        List<Mismatch> mismatches = new ArrayList<>();
        for (IntObjectCursor<IntHashSet> cursor : actual) {
            IntHashSet expected = expectedState.get(cursor.key);
            if (!cursor.value.equals(expected)) {
                mismatches.add(new Mismatch(cursor.key, expected, cursor.value));
            }
        }
        for (IntCursor cursor : EasyHppc.subtract(expectedState.keys(), actual.keys())) {
            int key = cursor.value;
            IntHashSet expected = expectedState.get(key);
            if (!expected.isEmpty()) {
                mismatches.add(new Mismatch(key, expected, null));
            }
        }
        return mismatches;
    }

    private record Mismatch(int key, @Nullable IntHashSet expected, @Nullable IntHashSet actual) {
        public @NotNull IntHashSet expected() {
            return EasyObjects.firstNonNull(expected, IntHashSet::new);
        }

        public @NotNull IntHashSet actual() {
            return EasyObjects.firstNonNull(actual, IntHashSet::new);
        }
    }
}
