package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SimpleTableField extends TableField {
    private final Column column;

    public SimpleTableField(@NotNull ModelField field,
                            boolean primaryKey,
                            @Nullable TableSchema foreignKey,
                            @Nullable AdapterInfo adapterInfo,
                            @NotNull Column column) {
        super(field, primaryKey, foreignKey, adapterInfo);
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
