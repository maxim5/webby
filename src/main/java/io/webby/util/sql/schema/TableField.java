package io.webby.util.sql.schema;

import io.webby.util.sql.api.FollowReferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class TableField implements WithColumns, WithPrefixedColumns {
    protected final TableSchema parent;
    protected final ModelField field;
    protected final boolean primaryKey;
    protected final AdapterInfo adapterInfo;

    public TableField(@NotNull TableSchema parent,
                      @NotNull ModelField field,
                      boolean primaryKey,
                      @Nullable AdapterInfo adapterInfo) {
        this.parent = parent;
        this.field = field;
        this.primaryKey = primaryKey;
        this.adapterInfo = adapterInfo;
    }

    public @NotNull TableSchema parent() {
        return parent;
    }

    public @NotNull String javaName() {
        return field.name();
    }

    public @NotNull String javaGetter() {
        return field.getter();
    }

    public @NotNull Class<?> javaType() {
        return field.type();
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isForeignKey() {
        return false;
    }

    public boolean isNativelySupportedType() {
        return adapterInfo == null;
    }

    public boolean isCustomSupportType() {
        return !isNativelySupportedType();
    }

    public @Nullable AdapterInfo adapterInfo() {
        return adapterInfo;
    }

    @Override
    public @NotNull List<PrefixedColumn> columns(@NotNull FollowReferences follow) {
        return columns().stream().map(column -> column.prefixed(parent.sqlName())).toList();
    }
}
