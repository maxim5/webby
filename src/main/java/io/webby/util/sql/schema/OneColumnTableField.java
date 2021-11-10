package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class OneColumnTableField extends TableField {
    private final Column column;

    public OneColumnTableField(@NotNull ModelField field,
                               boolean primaryKey,
                               @Nullable AdapterInfo adapterInfo,
                               @NotNull Column column) {
        super(field, primaryKey, adapterInfo);
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
