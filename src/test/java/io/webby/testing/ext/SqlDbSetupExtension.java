package io.webby.testing.ext;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.webby.db.sql.ConnectionPool;
import io.webby.db.sql.SqlSettings;
import io.webby.orm.api.Connector;
import io.webby.orm.api.Engine;
import io.webby.orm.api.debug.DebugRunner;
import io.webby.testing.TestingModules;
import io.webby.testing.TestingProps;
import io.webby.util.base.Rethrow.Runnables;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.logging.Level;

public class SqlDbSetupExtension implements AfterAllCallback, BeforeEachCallback, AfterEachCallback, Connector, DebugRunner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final SqlSettings settings;
    private final Connection connection;
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
                savepoint = null;
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
