package io.webby.orm.arch;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class MultiColumnTableField extends TableField {
    private final ImmutableList<Column> columns;

    public MultiColumnTableField(@NotNull TableArch parent,
                                 @NotNull ModelField field,
                                 boolean primaryKey,
                                 @NotNull AdapterApi adapterApi,
                                 @NotNull ImmutableList<Column> columns) {
        super(parent, field, primaryKey, adapterApi);
        this.columns = columns;
    }

    @Override
    public @NotNull ImmutableList<Column> columns() {
        return columns;
    }
}
