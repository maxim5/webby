package io.webby.testing.db.sql;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.webby.app.Settings;
import io.webby.db.sql.DDL;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.DbAdmin;
import io.webby.orm.api.TableMeta;
import io.webby.orm.api.query.DropTableQuery;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static io.webby.util.base.Unchecked.rethrow;

public class TestingPersistentDbTableCleaner {
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
                            DbAdmin admin = DbAdmin.ofFixed(runner);
                            for (TableMeta meta : getAllTables()) {
                                admin.dropTable(DropTableQuery.of(meta).ifExists().build());
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
