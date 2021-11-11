package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TableField implements WithColumns {
    protected final ModelField field;
    protected final boolean primaryKey;
    protected final AdapterInfo adapterInfo;

    public TableField(@NotNull ModelField field,
                      boolean primaryKey,
                      @Nullable AdapterInfo adapterInfo) {
        this.field = field;
        this.primaryKey = primaryKey;
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
}
