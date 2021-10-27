package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class SimpleTableField extends TableField {
    private final Column column;

    public SimpleTableField(@NotNull Field javaField,
                            @NotNull Method javaGetter,
                            boolean primaryKey,
                            @Nullable TableSchema foreignKey,
                            boolean nativelySupportedType,
                            @NotNull Column column) {
        super(javaField, javaGetter, primaryKey, foreignKey, nativelySupportedType);
        this.column = column;
    }

    public @NotNull Column column() {
        return column;
    }

    @Override
    public @NotNull List<Column> columns() {
        return Collections.singletonList(column);
    }

    @Override
    public int columnsNumber() {
        return 1;
    }
}
