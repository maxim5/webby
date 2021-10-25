package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class TableField implements WithColumns {
    protected final Field javaField;
    protected final Method javaGetter;
    protected final boolean primaryKey;
    protected final TableSchema foreignKey;
    private final boolean customType;

    public TableField(@NotNull Field javaField,
                      @NotNull Method javaGetter,
                      boolean primaryKey,
                      @Nullable TableSchema foreignKey,
                      boolean customType) {
        assert !primaryKey || foreignKey == null : "Field can't be PK and FK: %s".formatted(foreignKey);

        this.javaField = javaField;
        this.javaGetter = javaGetter;
        this.primaryKey = primaryKey;
        this.foreignKey = foreignKey;
        this.customType = customType;
    }

    public @NotNull Field javaField() {
        return javaField;
    }

    public @NotNull Method javaGetter() {
        return javaGetter;
    }

    public @NotNull Class<?> javaType() {
        return javaField.getType();
    }

    public @NotNull String javaName() {
        return javaField.getName();
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

    public boolean isCustomType() {
        return customType;
    }
}
