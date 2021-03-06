package io.webby.orm.arch;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Immutable
public class OneColumnTableField extends TableField {
    private final Column column;

    public OneColumnTableField(@NotNull TableArch parent,
                               @NotNull ModelField field,
                               boolean primaryKey,
                               @Nullable AdapterApi adapterApi,
                               @NotNull Column column) {
        super(parent, field, primaryKey, adapterApi);
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
