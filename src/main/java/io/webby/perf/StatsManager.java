package io.webby.perf;

import io.webby.db.kv.impl.DbStatsListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.db.kv.impl.DbStatsListener.OpContext;

public class StatsManager {
    private static final OpContext EMPTY_CONTEXT = () -> {};

    public @NotNull DbStatsListener getDbListener() {
        return new DbStatsListener() {
            @Override
            public @NotNull OpContext report(@NotNull Op op) {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    return () -> stats.unlock(op.statKey(), 1, null);
                }
                return EMPTY_CONTEXT;
            }

            @Override
            public @NotNull OpContext reportKey(@NotNull Op op, @NotNull Object key) {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    return () -> stats.unlock(op.statKey(), 1, key);
                }
                return EMPTY_CONTEXT;
            }

            @Override
            public @NotNull OpContext reportKeys(@NotNull Op op, @NotNull List<?> keys) {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    return () -> stats.unlock(op.statKey(), keys.size(), keys);
                }
                return EMPTY_CONTEXT;
            }
        };
    }
}
