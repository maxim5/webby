package io.spbx.webby.db.sql;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.spbx.orm.api.Connector;
import io.spbx.orm.api.QueryRunner;
import io.spbx.orm.api.TableMeta;
import io.spbx.orm.api.query.AlterTableAddForeignKeyQuery;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.webby.app.GuiceCompleteEvent;
import io.spbx.webby.app.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;

public class DDL {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Settings settings;
    private final TableManager manager;
    private final EventBus eventBus;

    @Inject
    public DDL(@NotNull Settings settings, @NotNull TableManager manager, @NotNull EventBus eventBus) {
        assert settings.storageSettings().isSqlEnabled() : "SQL storage is disabled";
        this.settings = settings;
        this.manager = manager;
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Subscribe
    public void guiceComplete(@NotNull GuiceCompleteEvent event) {
        log.at(Level.FINE).log("Getting SQL ready...");
        checkSqlReady();
        log.at(Level.FINE).log("SQL ready!");
        eventBus.post(new SqlReadyEvent());
    }

    protected void checkSqlReady() {
        if (settings.isDevMode()) {
            prepareForDev();
        } else {
            prepareForProd();
        }
    }

    protected void prepareForDev() {
        createAllTablesIfNotExist();
    }

    protected void prepareForProd() {
        // check tables exist?
    }

    private void createAllTablesIfNotExist() {
        if (settings.isProdMode()) {
            log.at(Level.WARNING).log("Automatic SQL table creation called in production");
        }

        // Table creation with foreign references may require the referenced table to be created first.
        // Also, there could potentially be cycled. Hence, FK creation is done separately.
        runner().adminTx().run(admin -> {
            // Table creation with foreign references may require the referenced table to be created first.
            // Also, there could potentially be cycled. Hence, FK creation is done separately.
            for (TableMeta table : getAllTables()) {
                admin.createTable(CreateTableQuery.of(table).ifNotExists().withEnforceForeignKey(false));
            }
            for (TableMeta table : getAllTables()) {
                admin.alterTable(AlterTableAddForeignKeyQuery.of(table));
            }
        });
    }

    protected @NotNull List<TableMeta> getAllTables() {
        return manager.getAllTables();
    }

    protected @NotNull Connector connector() {
        return manager.connector();
    }

    protected @NotNull QueryRunner runner() {
        return connector().runner();
    }
}
