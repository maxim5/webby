package io.spbx.webby.testing.db.sql;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.spbx.orm.api.TableMeta;
import io.spbx.orm.api.query.DropTableQuery;
import io.spbx.webby.app.Settings;
import io.spbx.webby.db.sql.DDL;
import io.spbx.webby.db.sql.TableManager;
import org.jetbrains.annotations.NotNull;

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
                    runner().adminTx().run(admin -> {
                        for (TableMeta table : getAllTables()) {
                            admin.ignoringForeignKeyChecks().dropTable(DropTableQuery.of(table).ifExists().cascade());
                        }
                    });
                }
            };
        }
    }
}
