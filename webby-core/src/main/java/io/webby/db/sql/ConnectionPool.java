package io.webby.db.sql;

import com.google.inject.Inject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.orm.api.Engine;
import io.webby.orm.api.HasEngine;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool implements HasEngine {
    private final HikariDataSource dataSource;

    @Inject
    public ConnectionPool(@NotNull Settings settings, @NotNull Lifetime lifetime) {
        assert settings.storageSettings().isSqlEnabled() : "SQL storage is disabled";
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
            return Unchecked.rethrow(e);
        }
    }

    @Override
    public @NotNull Engine engine() {
        return SqlSettings.parseEngineFromUrl(dataSource.getJdbcUrl());
    }

    public boolean isRunning() {
        return dataSource.isRunning();
    }

    @Override
    public String toString() {
        return "ConnectionPool{url=%s}".formatted(dataSource.getJdbcUrl());
    }
}
