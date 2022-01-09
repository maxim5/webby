package io.webby.testing.db.sql;

import com.google.common.eventbus.EventBus;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.webby.app.Settings;
import io.webby.db.sql.DDL;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

import static io.webby.util.base.Unchecked.rethrow;

public class TestingPersistentDbTableCleaner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject
    public TestingPersistentDbTableCleaner(@NotNull Settings settings, @NotNull EventBus eventBus, @NotNull Injector injector) {
        if (settings.storageSettings().isSqlEnabled()) {
            eventBus.unregister(injector.getInstance(DDL.class));
            new DDL(settings, injector.getInstance(TableManager.class), eventBus) {
                @Override
                protected void prepareForDev() {
                    dropAllTablesIfExist();
                    super.prepareForDev();
                }

                private void dropAllTablesIfExist() {
                    try {
                        connector().runner().runInTransaction(runner -> {
                            for (TableMeta meta : getAllTables()) {
                                log.at(Level.INFO).log("Deleting SQL table if exists: `%s`...", meta.sqlTableName());
                                String query = "DROP TABLE IF EXISTS %s".formatted(meta.sqlTableName());
                                runner.runUpdate(query);
                            }
                        });
                    } catch (SQLException e) {
                        rethrow(e);
                    }
                }
            };
        }
    }
}
