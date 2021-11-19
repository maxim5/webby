package io.webby.db.sql;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.util.base.Rethrow;
import io.webby.util.lazy.ResettableAtomicLazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

@Singleton
public class ConnectionPool {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final ResettableAtomicLazy<ConnectionPool> SHARED_INSTANCE = new ResettableAtomicLazy<>();

    private final HikariDataSource dataSource;

    @Inject
    public ConnectionPool(@NotNull Settings settings, @NotNull Lifetime lifetime) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(settings.storageSettings().sqlSettingsOrDie().url());
        dataSource = new HikariDataSource(config);
        lifetime.onTerminate(dataSource);
        initializeStatic(this);
    }

    @VisibleForTesting
    protected ConnectionPool() {
        dataSource = new HikariDataSource();
    }

    public @NotNull Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            return Rethrow.rethrow(e);
        }
    }

    @Override
    public String toString() {
        return "ConnectionPool{url=%s}".formatted(dataSource.getJdbcUrl());
    }

    public static @NotNull ConnectionPool unsafePool() {
        return SHARED_INSTANCE.getOrDie();
    }

    private static void initializeStatic(@NotNull ConnectionPool instance) {
        assert !instance.dataSource.isClosed() : "Can't initialize from a closed pool: %s".formatted(instance);

        if (SHARED_INSTANCE.isInitialized()) {
            ConnectionPool currentPool = SHARED_INSTANCE.getOrDie();
            boolean isClosed = currentPool.dataSource.isClosed();
            if (isClosed) {
                SHARED_INSTANCE.reinitializeOrDie(instance);
                log.at(Level.WARNING).log(
                    "Replaced the closed connection pool with a new one: old=`%s`, new=`%s`",
                    currentPool, instance
                );
            } else {
                log.at(Level.SEVERE).log(
                    "Current connection pool is still running, not replacing with a new one: current=`%s`, new=`%s`",
                    currentPool, instance
                );
            }
        } else {
            SHARED_INSTANCE.initializeOrDie(instance);
            log.at(Level.INFO).log("Initialized the connection pool: `%s`", instance);
        }
    }
}
