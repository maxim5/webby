package io.webby.db.kv.impl;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public interface DbStatsListener {
    @NotNull OpContext context();

    @NotNull OpContext report(@NotNull Op op);

    @NotNull OpContext reportKey(@NotNull Op op, @NotNull Object key);

    @NotNull OpContext reportKeyValue(@NotNull Op op, @NotNull Object key, @NotNull Object value);

    int DB_STAT = 0;
    enum Op {
        GET(DB_STAT + 1),
        SET(DB_STAT + 2),
        DELETE(DB_STAT + 3),
        SIZE(DB_STAT + 4),
        SCAN(DB_STAT + 5),
        IO(DB_STAT + 6);

        public static final Map<Integer, Op> VALUES = Arrays.stream(Op.values()).collect(Collectors.toMap(Op::statKey, e -> e));

        private final int statKey;

        Op(int statKey) {
            this.statKey = statKey;
        }

        public int statKey() {
            return statKey;
        }
    }

    interface OpContext extends Closeable {
        @Override
        void close();
    }
}
