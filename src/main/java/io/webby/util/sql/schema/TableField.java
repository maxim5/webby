package io.webby.util.sql.schema;

import io.webby.util.OneOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public abstract class TableField implements WithColumns {
    protected final TableSchema table;
    protected final OneOf<Field, Method> javaField;
    protected final boolean primaryKey;
    protected final TableSchema foreignKey;

    public TableField(@NotNull TableSchema table,
                      @NotNull OneOf<Field, Method> javaField,
                      boolean primaryKey,
                      @Nullable TableSchema foreignKey) {
        assert !primaryKey || foreignKey == null : "Field can't be PK and FK: %s".formatted(foreignKey);

        this.table = table;
        this.javaField = javaField;
        this.primaryKey = primaryKey;
        this.foreignKey = foreignKey;
    }

    public @NotNull TableSchema table() {
        return table;
    }

    public @NotNull OneOf<Field, Method> javaField() {
        return javaField;
    }

    public @NotNull Type javaType() {
        return javaField.hasFirst() ? javaField.first().getGenericType() : javaField.second().getGenericReturnType();
    }

    public @NotNull String javaName() {
        return javaField.hasFirst() ? javaField.first().getName() : javaField.second().getName();
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
}
