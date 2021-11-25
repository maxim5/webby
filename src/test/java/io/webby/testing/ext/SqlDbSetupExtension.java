package io.webby.testing.ext;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Module;
import io.webby.db.sql.ConnectionPool;
import io.webby.db.sql.SqlSettings;
import io.webby.orm.api.Engine;
import io.webby.testing.TestingModules;
import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Savepoint;
import java.util.logging.Level;

public class SqlDbSetupExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final String url;
    private final Connection connection;
    private final String schema;
    private Savepoint savepoint;

    public SqlDbSetupExtension(@NotNull String url) {
        this(url, "");
    }

    public SqlDbSetupExtension(@NotNull String url, @NotNull String schema) {
        this.url = url;
        this.connection = Rethrow.Suppliers.rethrow(() -> DriverManager.getConnection(url)).get();
        this.schema = schema;
        log.at(Level.FINE).log("SQL connection open: %s", url);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (schema.length() > 0) {
            connection.createStatement().executeUpdate(schema);
            log.at(Level.FINE).log("SQL schema applied:\n%s", schema);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        log.at(Level.FINE).log("SQL connection close");
        connection.close();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        connection.setAutoCommit(false);
        savepoint = connection.setSavepoint("before");
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        connection.rollback(savepoint);
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
}
