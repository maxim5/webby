package io.webby.db.count.vote;

import com.carrotsearch.hppc.IntHashSet;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.db.managed.BackgroundCacheCleaner;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.BaseTable;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class VotingCounterFactory {
    @Inject private Settings settings;
    @Inject private KeyValueFactory keyValueFactory;
    @Inject private Provider<TableManager> tableManagerProvider;
    @Inject private Lifetime lifetime;
    @Inject private EventBus eventBus;
    @Inject private BackgroundCacheCleaner cacheCleaner;

    public @NotNull VotingCounter getVotingCounter(@NotNull VotingOptions options) {
        VotingStorage storage = getStorage(options);
        VotingCounter counter = switch (options.counterType()) {
            case LOCK_BASED -> new LockBasedVotingCounter(storage, eventBus);
            case NON_BLOCKING -> new NonBlockingVotingCounter(storage, eventBus);
        };
        cacheCleaner.register(options.name(), counter);
        lifetime.onTerminate(counter);
        return counter;
    }

    private @NotNull VotingStorage getStorage(@NotNull VotingOptions options) {
        return options.store().mapToObj(storeType ->
            switch (storeType) {
                case KEY_VALUE_DB -> new KvVotingStorage(options.name(), getKeyValueDb(options.name()));
                case SQL_DB -> getTableVotingStorage(requireNonNull(options.tableSpec()));
            },
            storage -> storage
        );
    }

    private @NotNull KeyValueDb<Integer, IntHashSet> getKeyValueDb(@NotNull String name) {
        return keyValueFactory.getDb(DbOptions.of(name, Integer.class, IntHashSet.class));
    }

    private @NotNull TableVotingStorage getTableVotingStorage(@NotNull VotingTableSpec tableSpec) {
        assert settings.storageSettings().isSqlEnabled() : "SQL storage is disabled";
        BaseTable<?> table = tableManagerProvider.get().getTableOrDie(tableSpec.table());
        return new TableVotingStorage(table, tableSpec.keyColumn(), tableSpec.actorColumn(), tableSpec.valueColumn());
    }
}
