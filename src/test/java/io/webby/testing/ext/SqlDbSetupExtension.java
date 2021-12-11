package io.webby.testing.ext;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.webby.db.sql.ConnectionPool;
import io.webby.db.sql.SqlSettings;
import io.webby.orm.api.Engine;
import io.webby.testing.TestingModules;
import io.webby.util.base.Rethrow;
import io.webby.util.base.Rethrow.Runnables;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Savepoint;
import java.util.logging.Level;

public class SqlDbSetupExtension implements AfterAllCallback, BeforeEachCallback, AfterEachCallback {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final String url;
    private final Connection connection;
    private Savepoint savepoint;

    public SqlDbSetupExtension(@NotNull String url) {
        this.url = url;
        this.connection = Rethrow.Suppliers.rethrow(() -> DriverManager.getConnection(url)).get();
        log.at(Level.FINE).log("[SQL] Connection opened: %s", url);
    }

    public SqlDbSetupExtension(@NotNull SqlSettings settings) {
        this(settings.url());
    }

    public static @NotNull SqlDbSetupExtension fromProperties() {
        String engineValue = System.getProperty("test.sql.engine");
        if (engineValue != null) {
            Engine engine = Engine.fromJdbcType(engineValue.toLowerCase());
            assert engine != Engine.Unknown : "Failed to detect SQL engine: " + engineValue;
            log.at(Level.INFO).log("[SQL] Detected engine: %s", engine);
            SqlSettings settings = SqlSettings.inMemoryNotForProduction(engine);
            return new SqlDbSetupExtension(settings);
        }
        String url = System.getProperty("test.sql.url");
        if (url != null) {
            return new SqlDbSetupExtension(url);
        }
        log.at(Level.SEVERE).log("[SQL] Test SQL properties not found. Using SQLite");
        return new SqlDbSetupExtension(SqlSettings.SQLITE_IN_MEMORY);
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

    public @NotNull String getUrl() {
        return url;
    }

    public @NotNull SqlSettings getSettings() {
        return new SqlSettings(url);
    }

    public @NotNull Connection getConnection() {
        return connection;
    }

    public @NotNull Engine getEngine() {
        return singleConnectionPool().getEngine();
    }

    public @NotNull ConnectionPool singleConnectionPool() {
        return new ConnectionPool() {
            @Override
            public @NotNull Connection getConnection() {
                return connection;
            }
            @Override
            public @NotNull Engine getEngine() {
                return parseUrl(url);
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
