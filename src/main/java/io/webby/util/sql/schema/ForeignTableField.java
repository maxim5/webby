package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ForeignTableField extends TableField {
    private final TableSchema foreignTable;

    public ForeignTableField(@NotNull ModelField field, @NotNull TableSchema foreignTable) {
        super(field, false, null);
        this.foreignTable = foreignTable;
    }

    @Override
    public boolean isNativelySupportedType() {
        return false;
    }

    public boolean isForeignKey() {
        return foreignTable != null;
    }

    public @NotNull TableSchema getForeignTable() {
        return foreignTable;
    }

    @Override
    public @NotNull List<Column> columns() {
        return foreignTable.columns();
    }

    @Override
    public int columnsNumber() {
        return foreignTable.columnsNumber();
    }
}
