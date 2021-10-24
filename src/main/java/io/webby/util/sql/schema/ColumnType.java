package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ColumnType(@NotNull SqlDataFamily family, @Nullable String sqlType) {

}
