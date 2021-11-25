package io.webby.db.sql;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.orm.api.Engine;
import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class ConnectionPool {
    private final HikariDataSource dataSource;

    @Inject
    public ConnectionPool(@NotNull Settings settings, @NotNull Lifetime lifetime) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(settings.storageSettings().sqlSettingsOrDie().url());
        dataSource = new HikariDataSource(config);
        lifetime.onTerminate(dataSource);
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

    public @NotNull Engine getEngine() {
        return parseUrl(dataSource.getJdbcUrl());
    }

    public boolean isRunning() {
        return dataSource.isRunning();
    }

    @Override
    public String toString() {
        return "ConnectionPool{url=%s}".formatted(dataSource.getJdbcUrl());
    }

    protected static @NotNull Engine parseUrl(@Nullable String url) {
        if (url == null || !url.startsWith("jdbc:")) {
            return Engine.Unknown;
        }
        String[] parts = url.split(":");
        return parts.length > 1 ? Engine.fromJdbcType(parts[1]) : Engine.Unknown;
    }
}
