package io.webby.perf;

import io.webby.db.kv.impl.DbStatsListener;
import org.jetbrains.annotations.NotNull;

import static io.webby.db.kv.impl.DbStatsListener.*;

public class StatsManager {
    private static final OpContext EMPTY_CONTEXT = () -> {};

    public @NotNull DbStatsListener getDbListener() {
        return new DbStatsListener() {
            @Override
            public @NotNull OpContext context() {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    return stats::unlock;
                }
                return EMPTY_CONTEXT;
            }

            @Override
            public @NotNull OpContext report(@NotNull Op op) {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    stats.intStats().addTo(op.statKey(), 1);
                    return stats::unlock;
                }
                return EMPTY_CONTEXT;
            }

            @Override
            public @NotNull OpContext reportKey(@NotNull Op op, @NotNull Object key) {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    stats.intStats().addTo(op.statKey(), 1);
                    return stats::unlock;
                }
                return EMPTY_CONTEXT;
            }

            @Override
            public @NotNull OpContext reportKeyValue(@NotNull Op op, @NotNull Object key, @NotNull Object value) {
                RequestStatsCollector stats = LocalStatsHolder.getLocalStats();
                if (stats.lock()) {
                    stats.intStats().addTo(op.statKey(), 1);
                    return stats::unlock;
                }
                return EMPTY_CONTEXT;
            }
        };
    }
}
