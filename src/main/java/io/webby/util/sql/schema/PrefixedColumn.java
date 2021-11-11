package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

public record PrefixedColumn(@NotNull Column column, @NotNull String prefix) {
    public @NotNull String sqlName() {
        return column.sqlName();
    }

    public @NotNull ColumnType type() {
        return column.type();
    }

    @Override
    public String toString() {
        return "%s.%s".formatted(prefix, sqlName());
    }
}
