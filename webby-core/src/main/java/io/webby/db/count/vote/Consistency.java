package io.webby.db.count.vote;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.google.common.flogger.FluentLogger;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/*package*/ class Consistency {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static boolean checkStorageConsistency(@NotNull VotingStorage storage, @NotNull IntObjectMap<IntHashSet> state) {
        record Mismatch(int key, IntHashSet expected, IntHashSet actual) {
        }

        List<Mismatch> mismatches = new ArrayList<>();
        storage.loadBatch(state.keys(), (key, value) -> {
            IntHashSet expected = state.get(key);
            if (!value.equals(expected)) {
                mismatches.add(new Mismatch(key, expected, value));
            }
        });

        if (!mismatches.isEmpty()) {
            log.at(Level.WARNING).log(
                "Storage %s state changed for %d keys and will be overwritten from cache", storage, mismatches.size());
            int i = 0;
            for (Mismatch mismatch : mismatches) {
                IntHashSet addedInDb = EasyHppc.subtract(mismatch.actual, mismatch.expected);
                IntHashSet removedInDb = EasyHppc.subtract(mismatch.expected, mismatch.actual);
                log.at(Level.WARNING).log("key=%d added=%s removed=%s", mismatch.key, addedInDb, removedInDb);
                if (++i >= 10) {
                    break;
                }
            }
        }

        return true;
    }
}
