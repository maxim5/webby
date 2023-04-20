package io.webby.db.sql;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.GuiceCompleteEvent;
import io.webby.orm.api.Connector;
import io.webby.orm.api.DbAdmin;
import io.webby.orm.api.TableMeta;
import io.webby.orm.api.query.CreateTableQuery;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import static io.webby.util.base.Unchecked.rethrow;

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

        try {
            connector().runner().runInTransaction(runner -> {
                DbAdmin admin = DbAdmin.ofFixed(runner);
                for (TableMeta table : getAllTables()) {
                    admin.createTable(CreateTableQuery.of(table).ifNotExists());
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
