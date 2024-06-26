package io.spbx.webby.db.count.primitive;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.spbx.orm.api.BaseTable;
import io.spbx.webby.app.Settings;
import io.spbx.webby.common.Lifetime;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.KeyValueFactory;
import io.spbx.webby.db.managed.BackgroundCacheCleaner;
import io.spbx.webby.db.sql.TableManager;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class IntCounterFactory {
    @Inject private Settings settings;
    @Inject private KeyValueFactory keyValueFactory;
    @Inject private Provider<TableManager> tableManagerProvider;
    @Inject private Lifetime lifetime;
    @Inject private EventBus eventBus;
    @Inject private BackgroundCacheCleaner cacheCleaner;

    public @NotNull IntCounter getIntCounter(@NotNull CountingOptions options) {
        IntCountStorage storage = getStorage(options);
        LockBasedIntCounter counter = new LockBasedIntCounter(storage, eventBus);
        cacheCleaner.register(options.name(), counter);
        lifetime.onTerminate(counter);
        return counter;
    }

    private @NotNull IntCountStorage getStorage(@NotNull CountingOptions options) {
        return switch (options.storeType()) {
            case KEY_VALUE_DB -> new KvCountStorage(keyValueFactory.getDb(DbOptions.of(options.name(), Integer.class, Integer.class)));
            case SQL_DB -> {
                assert settings.storageSettings().isSqlEnabled() : "SQL storage is disabled";
                CountingTableSpec tableSpec = requireNonNull(options.tableSpec());
                BaseTable<?> table = tableManagerProvider.get().getTableOrDie(tableSpec.table());
                yield new TableCountStorage(table, tableSpec.keyColumn(), tableSpec.valueColumn());
            }
        };
    }
}
