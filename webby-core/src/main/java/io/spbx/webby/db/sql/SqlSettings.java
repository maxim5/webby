package io.spbx.webby.db.sql;

import com.google.errorprone.annotations.Immutable;
import io.spbx.orm.api.Engine;
import io.spbx.orm.api.HasEngine;
import io.spbx.util.base.EasyStrings;
import io.spbx.util.base.Unchecked;
import io.spbx.util.props.PropertyMap;
import io.spbx.webby.app.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// More SQL URLs:
// SqlSettings.jdbcUrl(Engine.H2, "file:./.data/temp.h2")
// SqlSettings.jdbcUrl(SqlSettings.SQLITE, "file:.data/temp.sqlite.db?mode=memory&cache=shared")
// TODO: support more formats: https://www.baeldung.com/java-jdbc-url-format
@Immutable
public record SqlSettings(@NotNull String url, @Nullable String user, @Nullable String password) implements HasEngine {
    private static final String JDBC_URL_PREFIX = "jdbc:";
    private static final String H2_IN_MEMORY_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String SQLITE_IN_MEMORY_URL = "jdbc:sqlite::memory:";
    private static final String MYSQL_TEST_URL = "jdbc:mysql://localhost/test?rewriteBatchedStatements=true";
    private static final String MARIA_IN_MEMORY_URL = "jdbc:mariadb://localhost:0/test";

    public static final SqlSettings H2_IN_MEMORY = new SqlSettings(H2_IN_MEMORY_URL);
    public static final SqlSettings SQLITE_IN_MEMORY = new SqlSettings(SQLITE_IN_MEMORY_URL);
    public static final SqlSettings MYSQL_TEST = new SqlSettings(MYSQL_TEST_URL, "test", "test");
    public static final SqlSettings MARIA_IN_MEMORY = new SqlSettings(MARIA_IN_MEMORY_URL, "root", "");

    public SqlSettings(@NotNull String url) {
        this(url, null, null);
    }

    public static @Nullable SqlSettings fromProperties(@NotNull PropertyMap properties) {
        String url = properties.getOrNull(Settings.SQL_URL);
        String user = properties.getOrNull(Settings.SQL_USER);
        String password = properties.getOrNull(Settings.SQL_PASSWORD);
        return url != null ? new SqlSettings(url, user, password) : null;
    }

    @Override
    public @NotNull Engine engine() {
        return parseEngineFromUrl(url);
    }

    public @Nullable String host() {
        URI uri = parseJdbcUrl(url);
        return uri != null ? uri.getHost() : null;
    }

    public int port() {
        URI uri = parseJdbcUrl(url);
        return uri != null ? uri.getPort() : -1;
    }

    public @Nullable String databaseName() {
        URI uri = parseJdbcUrl(url);
        return uri != null ? EasyStrings.removePrefix(uri.getPath(), "/") : null;
    }

    public @NotNull SqlSettings withUrl(@NotNull String url) {
        return new SqlSettings(url, user, password);
    }

    public @NotNull SqlSettings withCreds(@Nullable String user, @Nullable String password) {
        return new SqlSettings(url, user, password);
    }

    public @NotNull SqlSettings withoutCreds() {
        return withCreds(null, null);
    }

    private static @NotNull String jdbcUrl(@NotNull Engine engine, @NotNull String file) {
        assert engine != Engine.Unknown : "Can't make JDBC url from unknown DB engine";
        return "jdbc:%s:%s".formatted(engine.jdbcType(), file);
    }

    public static @NotNull Engine parseEngineFromUrl(@Nullable String url) {
        if (url == null || !url.startsWith(JDBC_URL_PREFIX)) {
            return Engine.Unknown;
        }
        String[] parts = url.split(":");
        return parts.length > 1 ? Engine.fromJdbcType(parts[1]) : Engine.Unknown;
    }

    // https://stackoverflow.com/questions/9287052/how-to-parse-a-jdbc-url-to-get-hostname-port-etc
    public static @Nullable URI parseJdbcUrl(@Nullable String url) {
        if (url == null || !url.startsWith(JDBC_URL_PREFIX)) {
            return null;
        }
        String uri = EasyStrings.removePrefix(url, JDBC_URL_PREFIX);
        return URI.create(uri);
    }

    public static @NotNull SqlSettings inMemoryForDevOnly(@NotNull Engine engine) {
        return switch (engine) {
            case H2 -> H2_IN_MEMORY;
            case SQLite -> SQLITE_IN_MEMORY;
            case MariaDB -> MARIA_IN_MEMORY;
            default -> throw new IllegalArgumentException("In-memory not supported for DB engine: " + engine);
        };
    }

    public static @NotNull Connection connectForDevOnly(@NotNull SqlSettings settings) {
        try {
            if (settings.user != null) {
                return DriverManager.getConnection(settings.url, settings.user, settings.password);
            }
            return DriverManager.getConnection(settings.url);
        } catch (SQLException e) {
            return Unchecked.rethrow(e);
        }
    }
}
