package io.webby.db.sql;

import io.webby.orm.api.Engine;
import org.jetbrains.annotations.NotNull;

public record SqlSettings(@NotNull String url) {
    public static final String H2_IN_MEMORY = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    public static final String SQLITE_IN_MEMORY = jdbcUrl(Engine.SQLite, ":memory:");

    public static String jdbcUrl(@NotNull Engine engine, @NotNull String file) {
        assert engine != Engine.Unknown : "Can't make JDBC url from unknown DB engine";
        return "jdbc:%s:%s".formatted(engine.jdbcType(), file);
    }

    public static @NotNull SqlSettings inMemoryNotForProduction(@NotNull Engine engine) {
        return switch (engine) {
            case H2 -> new SqlSettings(H2_IN_MEMORY);
            case SQLite -> new SqlSettings(SQLITE_IN_MEMORY);
            default -> throw new IllegalArgumentException("In-memory not supported for DB engine: " + engine);
        };
    }
}
