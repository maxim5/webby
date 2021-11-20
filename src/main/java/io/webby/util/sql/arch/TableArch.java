package io.webby.util.sql.arch;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.webby.util.lazy.AtomicLazyList;
import io.webby.util.sql.api.ReadFollow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.webby.util.sql.api.ReadFollow.FOLLOW_ALL;
import static io.webby.util.sql.api.ReadFollow.FOLLOW_ONE_LEVEL;

@Immutable
public record TableArch(@NotNull String sqlName,
                        @NotNull String javaName,
                        @NotNull String modelName,
                        @NotNull Class<?> modelClass,
                        @NotNull AtomicLazyList<TableField> fieldsRef) implements JavaNameHolder, WithColumns, WithPrefixedColumns {
    public @NotNull ImmutableList<TableField> fields() {
        return fieldsRef.getOrDie();
    }

    @Override
    public @NotNull String packageName() {
        return modelClass.getPackageName();
    }

    public boolean hasPrimaryKeyField() {
        return fields().stream().anyMatch(TableField::isPrimaryKey);
    }

    public @Nullable TableField primaryKeyField() {
        return fields().stream().filter(TableField::isPrimaryKey).findFirst().orElse(null);
    }

    public boolean hasForeignKeyField() {
        return fields().stream().anyMatch(TableField::isForeignKey);
    }

    // TODO: lazy cache
    public @NotNull List<ForeignTableField> foreignFields(@NotNull ReadFollow follow) {
        return switch (follow) {
            case NO_FOLLOW -> List.of();
            case FOLLOW_ONE_LEVEL -> fields().stream().filter(TableField::isForeignKey).map(field -> (ForeignTableField) field).toList();
            case FOLLOW_ALL -> foreignFields(FOLLOW_ONE_LEVEL).stream()
                    .flatMap(field -> Stream.concat(Stream.of(field), field.getForeignTable().foreignFields(FOLLOW_ALL).stream()))
                    .toList();
        };
    }

    @Override
    public @NotNull List<Column> columns() {
        return columns(tableField -> true);
    }

    public @NotNull List<Column> columns(@NotNull Predicate<TableField> fieldsFilter) {
        return fields().stream().filter(fieldsFilter).map(WithColumns::columns).flatMap(List::stream).toList();
    }

    @Override
    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow) {
        return columns(follow, tableField -> true);
    }

    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow, @NotNull Predicate<TableField> fieldsFilter) {
        return fields().stream().filter(fieldsFilter).flatMap(field -> field.columns(follow).stream()).toList();
    }

    /*package*/ void initializeOrDie(@NotNull ImmutableList<TableField> fields) {
        fieldsRef.initializeOrDie(fields);
    }
}
