package io.webby.util.sql.schema;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiColumnTableField extends TableField {
    private final ImmutableList<Column> columns;

    public MultiColumnTableField(@NotNull ModelField field,
                                 boolean primaryKey,
                                 @Nullable TableSchema foreignKey,
                                 @NotNull AdapterInfo adapterInfo,
                                 @NotNull ImmutableList<Column> columns) {
        super(field, primaryKey, foreignKey, adapterInfo);
        this.columns = columns;
    }

    @Override
    public @NotNull ImmutableList<Column> columns() {
        return columns;
    }
}
