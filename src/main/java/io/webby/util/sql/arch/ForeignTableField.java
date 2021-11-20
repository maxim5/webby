package io.webby.util.sql.arch;

import com.google.errorprone.annotations.Immutable;
import io.webby.util.collect.EasyIterables;
import io.webby.util.sql.api.ReadFollow;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.util.sql.api.ReadFollow.FOLLOW_ALL;
import static io.webby.util.sql.api.ReadFollow.NO_FOLLOW;

@Immutable
public class ForeignTableField extends TableField {
    private final TableArch foreignTable;
    private final Column foreignKeyColumn;

    public ForeignTableField(@NotNull TableArch parent,
                             @NotNull ModelField field,
                             @NotNull TableArch foreignTable,
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

    public @NotNull TableArch getForeignTable() {
        return foreignTable;
    }

    @Override
    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow) {
        List<PrefixedColumn> fkColumns = List.of(foreignKeyColumn.prefixed(parent.sqlName()));
        return switch (follow) {
            case NO_FOLLOW -> fkColumns;
            case FOLLOW_ONE_LEVEL -> EasyIterables.concat(fkColumns, foreignTable.columns(NO_FOLLOW));
            case FOLLOW_ALL -> EasyIterables.concat(fkColumns, foreignTable.columns(FOLLOW_ALL));
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
