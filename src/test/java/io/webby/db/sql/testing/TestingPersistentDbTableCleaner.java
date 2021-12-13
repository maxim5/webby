package io.webby.db.sql.testing;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.webby.app.Settings;
import io.webby.common.GuiceCompleteEvent;
import io.webby.db.sql.TableManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

import static io.webby.util.base.Rethrow.rethrow;

public class TestingPersistentDbTableCleaner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private final TableManager tableManager;

    @Inject
    public TestingPersistentDbTableCleaner(@NotNull Settings settings,
                                           @NotNull EventBus eventBus,
                                           @NotNull Provider<TableManager> tableManagerProvider) {
        if (settings.storageSettings().isSqlStorageEnabled()) {
            tableManager = tableManagerProvider.get();
            eventBus.register(this);
        } else {
            tableManager = null;
        }
    }

    @Subscribe
    public void guiceComplete(@NotNull GuiceCompleteEvent event) {
        dropAllTablesIfExist();
    }

    private void dropAllTablesIfExist() {
        assert tableManager != null;
        try {
            for (String tableName: tableManager.allTableNames()) {
                log.at(Level.INFO).log("Deleting SQL table if exists: `%s`...", tableName);
                String query = "DROP TABLE IF EXISTS %s".formatted(tableName);
                tableManager.connector().runner().runUpdate(query);
            }
        } catch (SQLException e) {
            rethrow(e);
        }
    }
}
