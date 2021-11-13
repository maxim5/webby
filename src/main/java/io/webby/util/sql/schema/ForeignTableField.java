package io.webby.util.sql.schema;

import io.webby.util.sql.api.ReadFollow;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ForeignTableField extends TableField {
    private final TableSchema foreignTable;
    private final Column foreignKeyColumn;

    public ForeignTableField(@NotNull TableSchema parent,
                             @NotNull ModelField field,
                             @NotNull TableSchema foreignTable,
                             @NotNull Column foreignKeyColumn) {
        super(parent, field, false, null);
        this.foreignTable = foreignTable;
        this.foreignKeyColumn = foreignKeyColumn;
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
    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow) {
        return switch (follow) {
            case NO_FOLLOW -> List.of(foreignKeyColumn.prefixed(parent.sqlName()));
            case ONE_LEVEL -> foreignTable.columns(ReadFollow.NO_FOLLOW);
            case FOLLOW_ALL -> foreignTable.columns(ReadFollow.FOLLOW_ALL);
        };
    }

    public @NotNull Column foreignKeyColumn() {
        return foreignKeyColumn;
    }

    @Override
    public @NotNull List<Column> columns() {
        return List.of(foreignKeyColumn);
    }

    @Override
    public int columnsNumber() {
        return 1;
    }
}
