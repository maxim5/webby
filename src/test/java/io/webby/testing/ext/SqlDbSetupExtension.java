package io.webby.testing.ext;

import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.webby.db.sql.ConnectionPool;
import io.webby.db.sql.SqlSettings;
import io.webby.orm.api.Connector;
import io.webby.orm.api.DebugSql;
import io.webby.orm.api.Engine;
import io.webby.testing.TestingModules;
import io.webby.util.base.Rethrow;
import io.webby.util.base.Rethrow.Runnables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;

public class SqlDbSetupExtension implements AfterAllCallback, BeforeEachCallback, AfterEachCallback, Connector {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final SqlSettings settings;
    private final Connection connection;
    private Savepoint savepoint;

    public SqlDbSetupExtension(@NotNull SqlSettings settings) {
        this.settings = settings;
        this.connection = Rethrow.Suppliers.rethrow(() -> DriverManager.getConnection(settings.url())).get();
        log.at(Level.FINE).log("[SQL] Connection opened: %s", settings.url());
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
            return new SqlDbSetupExtension(new SqlSettings(url));
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

    public @NotNull List<DebugSql.Row> runQuery(@NotNull String query, @Nullable Object @NotNull ... params) {
        try (PreparedStatement statement = runner().prepareQuery(query, params);
             ResultSet resultSet = statement.executeQuery()) {
            return DebugSql.toDebugRows(resultSet);
        } catch (SQLException e) {
            return Rethrow.rethrow(e);
        }
    }

    public @NotNull String runQueryToString(@NotNull String query, @Nullable Object @NotNull ... params) {
        return DebugSql.toDebugString(runQuery(query, params));
    }

    @CanIgnoreReturnValue
    public int runUpdate(@NotNull String sql, @Nullable Object @NotNull ... params) {
        try {
            return runner().runUpdate(sql, params);
        } catch (SQLException e) {
            return Rethrow.rethrow(e);
        }
    }
}
