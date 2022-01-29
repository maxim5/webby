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
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.TestingModules;
import io.webby.testing.TestingProps;
import io.webby.util.base.Unchecked.Runnables;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.*;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.logging.Level;

public class SqlDbSetupExtension implements BeforeAllCallback, AfterAllCallback,
                                            BeforeEachCallback, AfterEachCallback,
                                            Connector, DebugRunner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final SqlSettings settings;
    private final Connection connection;
    private TableMeta table;
    private Savepoint savepoint;

    public SqlDbSetupExtension(@NotNull SqlSettings settings) {
        this.settings = settings;
        this.connection = SqlSettings.connect(settings);
        log.at(Level.FINE).log("[SQL] Connection opened: %s", settings.url());
    }

    public static @NotNull SqlDbSetupExtension from(@NotNull SqlSettings settings) {
        return new SqlDbSetupExtension(settings);
    }

    public static @NotNull SqlDbSetupExtension fromProperties() {
        return from(TestingProps.propsSqlSettings());
    }

    public @NotNull SqlDbSetupExtension ofTable(@NotNull TableMeta table) {
        this.table = table;
        return this;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (table != null) {
            runUpdate(SqlSchemaMaker.makeDropTableQuery(table));
            runUpdate(SqlSchemaMaker.makeCreateTableQuery(engine(), table));
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        log.at(Level.FINE).log("[SQL] Connection close");
        connection.close();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        savepoint();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        rollback();
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
}
