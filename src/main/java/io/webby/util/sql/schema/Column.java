package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

public record Column(@NotNull String sqlName, @NotNull ColumnType type) {
}
