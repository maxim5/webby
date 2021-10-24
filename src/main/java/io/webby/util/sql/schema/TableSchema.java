package io.webby.util.sql.schema;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record TableSchema(@NotNull String sqlName,
                          @NotNull String javaName,
                          @NotNull Class<?> dataClass,
                          @NotNull List<TableField> fields) implements WithColumns {
    public boolean hasPrimaryKeyField() {
        return fields.stream().anyMatch(TableField::isPrimaryKey);
    }

    public @Nullable TableField primaryKeyField() {
        return fields.stream().filter(TableField::isPrimaryKey).findFirst().orElse(null);
    }

    public @NotNull Iterable<Column> columns() {
        return columns(tableField -> true);
    }

    public @NotNull List<Column> columns(@NotNull Predicate<TableField> fieldsFilter) {
        return fields.stream().filter(fieldsFilter).map(WithColumns::columns).flatMap(Streams::stream).toList();
    }
}
