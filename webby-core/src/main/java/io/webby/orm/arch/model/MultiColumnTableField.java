package io.webby.orm.arch.model;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.webby.orm.arch.Column;
import org.jetbrains.annotations.NotNull;

@Immutable
public class MultiColumnTableField extends TableField {
    private final ImmutableList<Column> columns;

    public MultiColumnTableField(@NotNull TableArch parent,
                                 @NotNull ModelField field,
                                 boolean primaryKey,
                                 boolean unique,
                                 boolean nullable,
                                 @NotNull AdapterApi adapterApi,
                                 @NotNull ImmutableList<Column> columns) {
        super(parent, field, primaryKey, unique, nullable, adapterApi);
        assert columns.size() > 1 : "MultiColumnTableField `%s` constructed from a single column %s".formatted(field.name(), columns);
        this.columns = columns;
    }

    @Override
    public @NotNull ImmutableList<Column> columns() {
        return columns;
    }

    @Override
    public boolean isMultiColumn() {
        return true;
    }

    @Override
    public boolean isSingleColumn() {
        return false;
    }
}
