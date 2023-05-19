package io.webby.testing.ext;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.webby.db.sql.ConnectionPool;
import io.webby.db.sql.SqlSettings;
import io.webby.orm.api.Connector;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import io.webby.orm.api.debug.DebugRunner;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.orm.api.query.DropTableQuery;
import io.webby.orm.api.query.TruncateTableQuery;
import io.webby.testing.TestingModules;
import io.webby.testing.TestingProps;
import io.webby.util.base.Unchecked.Runnables;
import io.webby.util.collect.OneOf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.List;
import java.util.logging.Level;

public class SqlDbExtension implements AfterAllCallback, BeforeEachCallback, AfterEachCallback, Connector, DebugRunner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final SqlSettings settings;
    private final Connection connection;
    private OneOf<SavepointHandler, ManualTablesHandler> handler;

    public SqlDbExtension(@NotNull SqlSettings settings) {
        this.settings = settings;
        this.connection = SqlSettings.connect(settings);
        this.handler = OneOf.ofFirst(new SavepointHandler(connection));
        log.at(Level.FINE).log("[SQL] Connection opened: %s", settings.url());
    }

    public static @NotNull SqlDbExtension from(@NotNull SqlSettings settings) {
        return new SqlDbExtension(settings);
    }

    public static @NotNull SqlDbExtension fromProperties() {
        return from(TestingProps.propsSqlSettings());
    }

    public @NotNull SqlDbExtension withSavepoints() {
        handler = OneOf.ofFirst(new SavepointHandler(connection));
        return this;
    }

    public @NotNull SqlDbExtension withManualCleanup(@NotNull TableMeta @NotNull ... tables) {
        handler = OneOf.ofSecond(new ManualTablesHandler(this, List.of(tables)));
        return this;
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        log.at(Level.FINE).log("[SQL] Connection close");
        connection.close();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        setUp();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        tearDown();
    }

    public void setUp() {
        handler.apply(SavepointHandler::savepoint, ManualTablesHandler::prepare);
    }

    public void tearDown() {
        handler.apply(SavepointHandler::rollback, ManualTablesHandler::cleanup);
    }

    public @NotNull SqlSettings settings() {
        return settings;
    }

    @Override
    public @NotNull Connection connection() {
        return connection;
    }

    @Override
    public @NotNull Engine engine() {
        return settings.engine();
    }

    public @NotNull ConnectionPool singleConnectionPool() {
        return new ConnectionPool() {
            @Override
            public @NotNull Connection getConnection() {
                return connection;
            }
            @Override
            public @NotNull Engine engine() {
                return settings.engine();
            }
            @Override
            public boolean isRunning() {
                return true;
            }
        };
    }

    public @NotNull Module singleConnectionPoolModule() {
        return TestingModules.instance(ConnectionPool.class, singleConnectionPool());
    }

    public @NotNull Module combinedTestingModule() {
        return Modules.combine(singleConnectionPoolModule(), TestingModules.PERSISTENT_DB_CLEANER_MODULE);
    }

    private static class SavepointHandler {
        private final Connection connection;
        private Savepoint savepoint;

        public SavepointHandler(@NotNull Connection connection) {
            this.connection = connection;
        }

        public void savepoint() {
            Runnables.runRethrow(() -> {
                log.at(Level.FINE).log("[SQL] Set savepoint");
                connection.setAutoCommit(false);
                savepoint = connection.setSavepoint("save");
            });
        }

        public void rollback() {
            Runnables.runRethrow(() -> {
                if (savepoint != null) {
                    log.at(Level.FINE).log("[SQL] Rollback to savepoint");
                    connection.rollback(savepoint);
                    connection.releaseSavepoint(savepoint);
                    savepoint = null;
                    connection.setAutoCommit(true);
                }
            });
        }
    }

    private record ManualTablesHandler(@NotNull Connector connector, @NotNull List<TableMeta> tables) {
        public void prepare() {
            connector.runner().adminTx().run(admin -> {
                for (TableMeta table : tables) {
                    admin.ignoringForeignKeyChecks().dropTable(DropTableQuery.of(table).ifExists().cascade());
                    admin.createTable(CreateTableQuery.of(table).ifNotExists());
                }
            });
        }

        public void cleanup() {
            connector.runner().adminTx().run(admin -> {
                for (TableMeta table : tables) {
                    admin.ignoringForeignKeyChecks().truncateTable(TruncateTableQuery.of(table));
                }
            });
        }
    }
}
