package io.webby.db.sql;

import org.jetbrains.annotations.NotNull;

public record SqlSettings(@NotNull String url) {
    public static final String H2 = "h2";
    public static final String SQLITE = "sqlite";

    public static final String IN_MEMORY = ":memory:";
    public static final String SQLITE_IN_MEMORY = jdbcUrl(SQLITE, IN_MEMORY);

    public static String jdbcUrl(@NotNull String engine, @NotNull String file) {
        return "jdbc:%s:%s".formatted(engine, file);
    }

    public static @NotNull SqlSettings inMemoryNotForProduction() {
        return new SqlSettings(SQLITE_IN_MEMORY);
    }
}
