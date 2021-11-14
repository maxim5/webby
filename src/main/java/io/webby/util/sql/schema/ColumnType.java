package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

public record ColumnType(@NotNull JdbcType jdbcType) {
    @Override
    public String toString() {
        return jdbcType.toString();
    }
}
