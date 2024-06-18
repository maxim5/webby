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
import io.spbx.util.base.Unchecked;
import io.spbx.util.base.Unchecked.Runnables;
import io.spbx.util.lazy.AtomicLazyRecycle;
import io.spbx.util.lazy.LazyRecycle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

import static java.util.Objects.requireNonNull;

public class SqlDbExtension implements BeforeAllCallback, AfterAllCallback,
                                       BeforeEachCallback, AfterEachCallback,
                                       Connector, DebugRunner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final SqlSettings settings;
    private final LazyRecycle<EmbeddedDb> embeddedDb = AtomicLazyRecycle.createUninitialized();
    private final ConnectionWrapper connection;
    private TestDataHandler testDataHandler;

    private SqlDbExtension(@NotNull SqlSettings rawSettings) {
        this.settings = LocalPortResolver.tryResolveSqlSettings(rawSettings);
        this.connection = new ConnectionWrapper(settings);
        this.testDataHandler = new SavepointHandler(this::connection);
    }

    public static @NotNull SqlDbExtension from(@NotNull SqlSettings settings) {
        return new SqlDbExtension(settings);
    }

    public static @NotNull SqlDbExtension fromProperties() {
        return from(TestingProps.propsSqlSettings());
    }

    public @NotNull SqlDbExtension withSavepoints() {
        testDataHandler = new SavepointHandler(this::connection);
        return this;
    }

    public @NotNull SqlDbExtension withManualCleanup(@NotNull TableMeta @NotNull ... tables) {
        testDataHandler = new ManualTablesHandler(this, List.of(tables));
        return this;
    }

    // JUnit 5 Lifecycle

    @Override
    public void beforeAll(ExtensionContext context) {
        embeddedDb.initializeOrDie(EmbeddedDb.getDb(settings).startup());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        connection.disconnect();
        embeddedDb.recycle(EmbeddedDb::shutdown);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        connection.connect();
        setupTestData();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        teardownTestData();
        connection.disconnect();
    }

    // Connection

    private static class ConnectionWrapper {
        private final SqlSettings settings;
        private @Nullable Connection connection;

        public ConnectionWrapper(@NotNull SqlSettings settings) {
            this.settings = settings;
            this.connection = null;
        }

        public @NotNull Connection current() {
            return requireNonNull(connection);
        }

        public void connect() {
            if (connection != null) {
                disconnect(connection);
            }
            connection = connect(settings);
        }

        public void disconnect() {
            if (connection != null) {
                disconnect(connection);
                connection = null;
            }
        }

        private static @NotNull Connection connect(@NotNull SqlSettings settings) {
            Connection connection = SqlSettings.connectNotForProduction(settings);
            log.at(Level.FINE).log("[SQL] Connection opened: %s", settings.url());
            return connection;
        }

        private static void disconnect(@NotNull Connection connection) {
            try {
                connection.close();
                log.at(Level.FINE).log("[SQL] Connection close");
            } catch (SQLException e) {
                Unchecked.rethrow(e);
            }
        }
    }

    // External API once connected

    public @NotNull SqlSettings settings() {
        return settings;
    }

    @Override
    public @NotNull Connection connection() {
        return connection.current();
    }

    @Override
    public @NotNull Engine engine() {
        return settings().engine();
    }

    private @NotNull ConnectionPool singleConnectionPool() {
        return new ConnectionPool() {
            @Override
            public @NotNull Connection getConnection() {
                return connection();
            }
            @Override
            public @NotNull Engine engine() {
                return settings().engine();
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

    // Test Data Handler

    public void setupTestData() {
        testDataHandler.setup();
    }

    public void teardownTestData() {
        testDataHandler.teardown();
    }

    private interface TestDataHandler {
        void setup();
        void teardown();
    }

    private static class SavepointHandler implements TestDataHandler {
        private final Supplier<Connection> connection;
        private Savepoint savepoint;

        public SavepointHandler(@NotNull Supplier<Connection> connection) {
            this.connection = connection;
        }

        @Override
        public void setup() {
            savepoint();
        }

        @Override
        public void teardown() {
            rollback();
        }

        private void savepoint() {
            Runnables.runRethrow(() -> {
                log.at(Level.FINE).log("[SQL] Set savepoint");
                connection.get().setAutoCommit(false);
                savepoint = connection.get().setSavepoint("save");
            });
        }

        private void rollback() {
            Runnables.runRethrow(() -> {
                if (savepoint != null) {
                    log.at(Level.FINE).log("[SQL] Rollback to savepoint");
                    connection.get().rollback(savepoint);
                    connection.get().releaseSavepoint(savepoint);
                    savepoint = null;
                    connection.get().setAutoCommit(true);
                }
            });
        }
    }

    private record ManualTablesHandler(@NotNull Connector connector,
                                       @NotNull List<TableMeta> tables) implements TestDataHandler {
        @Override
        public void setup() {
            prepare();
        }

        @Override
        public void teardown() {
            cleanup();
        }

        private void prepare() {
            connector.runner().adminTx().run(admin -> {
                for (TableMeta table : tables) {
                    admin.ignoringForeignKeyChecks().dropTable(DropTableQuery.of(table).ifExists().cascade());
                    admin.createTable(CreateTableQuery.of(table).ifNotExists());
                }
            });
        }

        private void cleanup() {
            connector.runner().adminTx().run(admin -> {
                for (TableMeta table : tables) {
                    admin.ignoringForeignKeyChecks().truncateTable(TruncateTableQuery.of(table));
                }
            });
        }
    }
}
