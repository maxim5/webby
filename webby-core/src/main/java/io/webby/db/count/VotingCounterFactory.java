package io.webby.db.count;

import com.carrotsearch.hppc.IntHashSet;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.db.count.impl.*;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.BaseTable;
import io.webby.orm.api.query.Column;
import io.webby.util.collect.OneOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VotingCounterFactory {
    @Inject private Settings settings;
    @Inject private KeyValueFactory keyValueFactory;
    @Inject private TableManager tables;
    @Inject private Lifetime lifetime;

    public @NotNull VotingCounter getVotingCounter(@NotNull VotingOptions options) {
        VotingStorage storage = getVotingStorage(options);
        VotingCounter counter = switch (options.counterType()) {
            case LOCK_BASED -> new LockBasedVotingCounter(storage);
            case NON_BLOCKING -> new NonBlockingVotingCounter(storage);
        };
        lifetime.onTerminate(counter);
        return counter;
    }

    private @NotNull VotingStorage getVotingStorage(@NotNull VotingOptions options) {
        return options.store().mapToObj(storeType ->
            switch (storeType) {
                case KEY_VALUE_DB -> new KvVotingStorage(getKeyValueDb(options.name()));
                case TABLE -> getTableVotingStorage(Objects.requireNonNull(options.tableSpec()));
            },
            storage -> storage
        );
    }

    private @NotNull KeyValueDb<Integer, IntHashSet> getKeyValueDb(@NotNull String name) {
        return keyValueFactory.getDb(DbOptions.of(name, Integer.class, IntHashSet.class));
    }

    private @NotNull TableVotingStorage getTableVotingStorage(@NotNull VoteTableSpec tableSpec) {
        BaseTable<?> table = tables.getTableOrDie(tableSpec.tableClass());
        return new TableVotingStorage(table, tableSpec.keyColumn(), tableSpec.actorColumn(), tableSpec.valueColumn());
    }

    public record VotingOptions(@NotNull String name,
                                @NotNull VotingCounterType counterType,
                                @NotNull OneOf<VotingStoreType, VotingStorage> store,
                                @Nullable VoteTableSpec tableSpec) {
        public static @NotNull VotingOptions ofKeyValue(@NotNull String name, @NotNull VotingCounterType counterType) {
            return new VotingOptions(name, counterType, OneOf.ofFirst(VotingStoreType.KEY_VALUE_DB), null);
        }

        public static @NotNull VotingOptions ofSqlTable(@NotNull String name,
                                                        @NotNull VotingCounterType counterType,
                                                        @NotNull VoteTableSpec tableSpec) {
            return new VotingOptions(name, counterType, OneOf.ofFirst(VotingStoreType.TABLE), tableSpec);
        }
    }

    // FIX[minor]: find by annotation?
    public record VoteTableSpec(@NotNull Class<? extends BaseTable<?>> tableClass,
                                @NotNull Column keyColumn,
                                @NotNull Column actorColumn,
                                @NotNull Column valueColumn) {
    }

    public enum VotingStoreType {
        TABLE,
        KEY_VALUE_DB,
    }

    public enum VotingCounterType {
        LOCK_BASED,
        NON_BLOCKING,
    }
}
