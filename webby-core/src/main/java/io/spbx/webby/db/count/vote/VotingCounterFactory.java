package io.spbx.webby.db.count.vote;

import com.carrotsearch.hppc.IntHashSet;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.spbx.orm.api.BaseTable;
import io.spbx.webby.app.Settings;
import io.spbx.webby.common.Lifetime;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.kv.KeyValueFactory;
import io.spbx.webby.db.managed.BackgroundCacheCleaner;
import io.spbx.webby.db.sql.TableManager;
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
