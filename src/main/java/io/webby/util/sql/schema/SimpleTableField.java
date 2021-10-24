package io.webby.util.sql.schema;

import io.webby.util.OneOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

public class SimpleTableField extends TableField {
    private final Column column;

    public SimpleTableField(@NotNull TableSchema table,
                            @NotNull OneOf<Field, Method> javaField,
                            boolean primaryKey,
                            @Nullable TableSchema foreignKey,
                            @NotNull Column column) {
        super(table, javaField, primaryKey, foreignKey);
        this.column = column;
    }

    @Override
    public int columnsNumber() {
        return 1;
    }

    public @NotNull Column column() {
        return column;
    }

    @Override
    public @NotNull Iterable<Column> columns() {
        return Collections.singletonList(column);
    }
}
