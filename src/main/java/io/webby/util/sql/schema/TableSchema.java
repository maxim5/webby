package io.webby.util.sql.schema;

import com.google.common.collect.ImmutableList;
import io.webby.util.lazy.AtomicLazyList;
import io.webby.util.sql.api.FollowReferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record TableSchema(@NotNull String sqlName,
                          @NotNull String javaName,
                          @NotNull String modelName,
                          @NotNull Class<?> modelClass,
                          @NotNull AtomicLazyList<TableField> fieldsRef) implements WithColumns, JavaNameHolder {
    public @NotNull ImmutableList<TableField> fields() {
        return fieldsRef.getOrDie();
    }

    public @NotNull String packageName() {
        return modelClass.getPackageName();
    }

    public boolean hasPrimaryKeyField() {
        return fields().stream().anyMatch(TableField::isPrimaryKey);
    }

    public @Nullable TableField primaryKeyField() {
        return fields().stream().filter(TableField::isPrimaryKey).findFirst().orElse(null);
    }

    public @NotNull List<Column> columns() {
        return columns(tableField -> true);
    }

    public @NotNull List<Column> columns(@NotNull Predicate<TableField> fieldsFilter) {
        return fields().stream().filter(fieldsFilter).map(WithColumns::columns).flatMap(List::stream).toList();
    }

    public @NotNull List<Column> columns(@NotNull FollowReferences follow) {
        return fields().stream().flatMap(field -> field.columns(follow).stream()).toList();
    }

    /*package*/ void initializeOrDie(@NotNull ImmutableList<TableField> fields) {
        fieldsRef.initializeOrDie(fields);
    }
}
