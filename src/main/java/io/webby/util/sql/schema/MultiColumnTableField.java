package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class MultiColumnTableField extends TableField {
    private final List<Column> columns;

    public MultiColumnTableField(@NotNull Field javaField,
                                 @NotNull Method javaGetter,
                                 boolean primaryKey,
                                 @Nullable TableSchema foreignKey,
                                 @NotNull AdapterInfo adapterInfo,
                                 @NotNull List<Column> columns) {
        super(javaField, javaGetter, primaryKey, foreignKey, adapterInfo);
        this.columns = columns;
    }

    @Override
    public @NotNull List<Column> columns() {
        return columns;
    }
}
