package io.webby.db.kv;

import com.google.common.flogger.FluentLogger;
import io.spbx.util.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class KeyValueAutoRetryInserter<K, V> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final KeyValueDb<K, V> db;
    private final Supplier<K> keyGenerator;
    private final int maxAttempts;

    public KeyValueAutoRetryInserter(@NotNull KeyValueDb<K, V> db, @NotNull Supplier<K> keyGenerator, int maxAttempts) {
        assert maxAttempts > 0 : "Invalid max attempts value: " + maxAttempts;
        this.db = db;
        this.keyGenerator = keyGenerator;
        this.maxAttempts = maxAttempts;
    }

    public @NotNull Pair<K, V> insertOrDie(@NotNull Function<K, V> valueProvider) {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            K key = keyGenerator.get();
            V value = valueProvider.apply(key);
            if (tryInsert(key, value)) {
                return Pair.of(key, value);
            }
            log.at(Level.WARNING).log("Failed to insert a (key, value) pair: attempt=%d, key=%s", attempt, key);
        }
        throw new RuntimeException("Too many failed attempts to insert (key, value) pair: " + maxAttempts);
    }

    private boolean tryInsert(@NotNull K key, @NotNull V value) {
        return db.putIfAbsent(key, value) == null;
    }
}
