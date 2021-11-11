package io.webby.util.sql.schema;

import io.webby.util.sql.api.FollowReferences;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ForeignTableField extends TableField implements WithPrefixedColumns {
    private final TableSchema foreignTable;
    private final Column foreignKeyColumn;

    public ForeignTableField(@NotNull ModelField field,
                             @NotNull TableSchema foreignTable,
                             @NotNull Column foreignKeyColumn) {
        super(field, false, null);
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
    public @NotNull List<PrefixedColumn> columns(@NotNull FollowReferences follow) {
        return switch (follow) {
            case NO_FOLLOW -> List.of(foreignKeyColumn.prefixed(foreignTable.sqlName()));
            case ONE_LEVEL -> foreignTable.columns(FollowReferences.NO_FOLLOW);
            case ALL -> foreignTable.columns(FollowReferences.ALL);
        };
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
