package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

public record Column(@NotNull String sqlName, @NotNull ColumnType type) {
    public @NotNull Column renamed(@NotNull String newName) {
        return new Column(newName, type);
    }
}
