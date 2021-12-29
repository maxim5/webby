package io.webby.db.sql;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.GuiceCompleteEvent;
import io.webby.orm.api.Connector;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import io.webby.orm.codegen.SqlSchemaMaker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import static io.webby.util.base.Rethrow.rethrow;

public class DDL {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Settings settings;
    private final TableManager manager;

    @Inject
    public DDL(@NotNull Settings settings, @NotNull TableManager manager, @NotNull EventBus eventBus) {
        assert settings.storageSettings().isSqlEnabled() : "SQL storage is disabled";
        this.settings = settings;
        this.manager = manager;
        eventBus.register(this);
    }

    protected DDL(@NotNull Settings settings, @Nullable TableManager manager) {
        this.settings = settings;
        this.manager = manager;
    }

    @Subscribe
    public void guiceComplete(@NotNull GuiceCompleteEvent event) {
        checkSqlReady();
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

        Engine engine = connector().engine();
        try {
            connector().runner().runInTransaction(runner -> {
                for (TableMeta meta : getAllTables()) {
                    log.at(Level.INFO).log("Creating SQL table if not exists: `%s`...", meta.sqlTableName());
                    String query = SqlSchemaMaker.makeCreateTableQuery(engine, meta);
                    runner.runUpdate(query);
                }
            });
        } catch (SQLException e) {
            rethrow(e);
        }
    }

    protected @NotNull List<TableMeta> getAllTables() {
        return manager.getAllTables();
    }

    protected @NotNull Connector connector() {
        return manager.connector();
    }
}
