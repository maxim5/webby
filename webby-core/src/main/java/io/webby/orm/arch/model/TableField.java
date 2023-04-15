package io.webby.orm.arch.model;

import com.google.errorprone.annotations.Immutable;
import io.webby.orm.api.ReadFollow;
import io.webby.orm.arch.Column;
import io.webby.orm.arch.HasColumns;
import io.webby.orm.arch.HasPrefixedColumns;
import io.webby.orm.arch.PrefixedColumn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Immutable
public abstract class TableField implements HasColumns, HasPrefixedColumns {
    protected final TableArch parent;
    protected final ModelField field;
    protected final boolean primaryKey;
    protected final boolean unique;
    protected final boolean nullable;
    protected final Defaults defaults;
    protected final AdapterApi adapterApi;

    public TableField(@NotNull TableArch parent,
                      @NotNull ModelField field,
                      boolean primaryKey,
                      boolean unique,
                      boolean nullable,
                      @NotNull Defaults defaults,
                      @Nullable AdapterApi adapterApi) {
        this.parent = parent;
        this.field = field;
        this.primaryKey = primaryKey;
        this.unique = unique;
        this.nullable = nullable;
        this.defaults = defaults;
        this.adapterApi = adapterApi;
    }

    public @NotNull TableArch parent() {
        return parent;
    }

    public @NotNull String javaName() {
        return field.name();
    }

    public @NotNull String javaAccessor() {
        return field.accessor();
    }

    public @NotNull Class<?> javaType() {
        return field.type();
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isNotNull() {
        return !nullable;
    }

    public boolean isForeignKey() {
        return false;
    }

    public @NotNull Defaults defaults() {
        return defaults;
    }

    public @Nullable String columnDefault(@NotNull Column column) {
        int index = columns().indexOf(column);
        assert index >= 0 : "The column not found in the field: field_columns=%s column=%s".formatted(columns(), column);
        return defaults.at(index);
    }

    public boolean isNativelySupportedType() {
        return adapterApi == null;
    }

    public boolean isCustomSupportType() {
        return !isNativelySupportedType();
    }

    public @Nullable AdapterApi adapterApi() {
        return adapterApi;
    }

    @Override
    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow) {
        return columns().stream().map(column -> column.prefixed(parent.sqlName())).toList();
    }

    @Override
    public String toString() {
        return "%s(%s::%s, primary:%s, unique:%s, null:%s, defaults:%s)".formatted(
            getClass().getSimpleName(),
            parent.javaName(),
            field.name(),
            primaryKey,
            unique,
            nullable,
            defaults
        );
    }
}
