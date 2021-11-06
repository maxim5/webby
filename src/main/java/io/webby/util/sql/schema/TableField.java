package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TableField implements WithColumns {
    protected final ModelField field;
    protected final boolean primaryKey;
    protected final TableSchema foreignKey;
    protected final AdapterInfo adapterInfo;

    public TableField(@NotNull ModelField field,
                      boolean primaryKey,
                      @Nullable TableSchema foreignKey,
                      @Nullable AdapterInfo adapterInfo) {
        assert !primaryKey || foreignKey == null : "Field can't be PK and FK: %s".formatted(foreignKey);

        this.field = field;
        this.primaryKey = primaryKey;
        this.foreignKey = foreignKey;
        this.adapterInfo = adapterInfo;
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
        return foreignKey != null;
    }

    public @Nullable TableSchema getForeignTable() {
        return foreignKey;
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
}
