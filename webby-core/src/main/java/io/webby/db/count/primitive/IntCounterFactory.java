package io.webby.db.count.primitive;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.BaseTable;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class IntCounterFactory {
    @Inject private Settings settings;
    @Inject private KeyValueFactory keyValueFactory;
    @Inject private Provider<TableManager> tableManagerProvider;
    @Inject private Lifetime lifetime;
    @Inject private EventBus eventBus;

    public @NotNull IntCounter getIntCounter(@NotNull CountingOptions options) {
        IntCountStorage storage = getStorage(options);
        LockBasedIntCounter counter = new LockBasedIntCounter(storage, eventBus);
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
