package io.webby.perf.stats.impl;

import io.webby.perf.stats.CodecStatsListener;
import io.webby.perf.stats.DbStatsListener;
import io.webby.perf.stats.Stat;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.perf.stats.DbStatsListener.OpContext;

public class StatsManager {
    private static final OpContext EMPTY_CONTEXT = () -> {};

    public @NotNull DbStatsListener newDbListener() {
        return new DbStatsListener() {
            @Override
            public @NotNull OpContext report(@NotNull Stat stat) {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    return () -> stats.unlock(stat.key(), 1, null);
                }
                return EMPTY_CONTEXT;
            }

            @Override
            public @NotNull OpContext reportKey(@NotNull Stat stat, @NotNull Object key) {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    return () -> stats.unlock(stat.key(), 1, key);
                }
                return EMPTY_CONTEXT;
            }

            @Override
            public @NotNull OpContext reportKeys(@NotNull Stat stat, @NotNull List<?> keys) {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    return () -> stats.unlock(stat.key(), keys.size(), keys);
                }
                return EMPTY_CONTEXT;
            }
        };
    }

    public @NotNull CodecStatsListener newCodecStatsListener() {
        return (stat, numBytes, elapsedMillis, hint) -> {
            RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
            stats.report(stat.key(), numBytes, elapsedMillis, hint);
        };
    }
}
