package io.webby.perf.stats;

import io.webby.util.EasyIterables;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.List;

public interface DbStatsListener {
    @NotNull OpContext report(@NotNull Stat stat);

    @NotNull OpContext reportKey(@NotNull Stat stat, @NotNull Object key);

    @NotNull OpContext reportKeys(@NotNull Stat stat, @NotNull List<?> keys);

    default @NotNull OpContext reportKeys(@NotNull Stat stat, @NotNull Iterable<?> keys) {
        return reportKeys(stat, EasyIterables.asList(keys));
    }

    default @NotNull OpContext reportKeys(@NotNull Stat stat, @NotNull Object[] keys) {
        return reportKeys(stat, List.of(keys));
    }

    interface OpContext extends Closeable {
        @Override
        void close();
    }
}
