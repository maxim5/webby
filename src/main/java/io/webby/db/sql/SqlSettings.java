package io.webby.db.sql;

import io.webby.orm.api.Engine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// More SQL URLs:
// SqlSettings.jdbcUrl(Engine.H2, "file:./.data/temp.h2")
// SqlSettings.jdbcUrl(SqlSettings.SQLITE, "file:.data/temp.sqlite.db?mode=memory&cache=shared")
public record SqlSettings(@NotNull String url) {
    private static final String H2_IN_MEMORY_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String SQLITE_IN_MEMORY_URL = "jdbc:sqlite::memory:";
    private static final String MYSQL_TEST_URL = "jdbc:mysql://localhost/test?user=test&password=test";

    public static final SqlSettings H2_IN_MEMORY = new SqlSettings(H2_IN_MEMORY_URL);
    public static final SqlSettings SQLITE_IN_MEMORY = new SqlSettings(SQLITE_IN_MEMORY_URL);
    public static final SqlSettings MYSQL_TEST = new SqlSettings(MYSQL_TEST_URL);

    public @NotNull Engine engine() {
        return parseEngineFromUrl(url);
    }

    public static @NotNull SqlSettings inMemoryNotForProduction(@NotNull Engine engine) {
        return switch (engine) {
            case H2 -> H2_IN_MEMORY;
            case SQLite -> SQLITE_IN_MEMORY;
            default -> throw new IllegalArgumentException("In-memory not supported for DB engine: " + engine);
        };
    }

    private static @NotNull String jdbcUrl(@NotNull Engine engine, @NotNull String file) {
        assert engine != Engine.Unknown : "Can't make JDBC url from unknown DB engine";
        return "jdbc:%s:%s".formatted(engine.jdbcType(), file);
    }

    public static @NotNull Engine parseEngineFromUrl(@Nullable String url) {
        if (url == null || !url.startsWith("jdbc:")) {
            return Engine.Unknown;
        }
        String[] parts = url.split(":");
        return parts.length > 1 ? Engine.fromJdbcType(parts[1]) : Engine.Unknown;
    }
}
