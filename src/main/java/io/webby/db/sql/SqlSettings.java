package io.webby.db.sql;

import org.jetbrains.annotations.NotNull;

public record SqlSettings(@NotNull String url) {
    public static final String SQLITE_IN_MEMORY = "jdbc:sqlite:%s".formatted(":memory:");

    public static @NotNull SqlSettings inMemoryNotForProduction() {
        return new SqlSettings(SQLITE_IN_MEMORY);
    }
}
