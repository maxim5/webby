package io.webby.util.sql.schema;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

public class MultiColumnTableField extends TableField {
    private final ImmutableList<Column> columns;

    public MultiColumnTableField(@NotNull TableSchema parent,
                                 @NotNull ModelField field,
                                 boolean primaryKey,
                                 @NotNull AdapterInfo adapterInfo,
                                 @NotNull ImmutableList<Column> columns) {
        super(parent, field, primaryKey, adapterInfo);
        this.columns = columns;
    }

    @Override
    public @NotNull ImmutableList<Column> columns() {
        return columns;
    }
}
