package io.webby.orm.arch.model;

import com.google.errorprone.annotations.Immutable;
import io.webby.orm.api.ReadFollow;
import io.webby.orm.arch.Column;
import io.webby.orm.arch.PrefixedColumn;
import io.webby.util.collect.ListBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.orm.api.ReadFollow.FOLLOW_ALL;
import static io.webby.orm.api.ReadFollow.NO_FOLLOW;

@Immutable
public class ForeignTableField extends TableField {
    private final TableArch foreignTable;
    private final Column foreignKeyColumn;

    public ForeignTableField(@NotNull TableArch parent,
                             @NotNull ModelField field,
                             @NotNull TableArch foreignTable,
                             @NotNull Column foreignKeyColumn) {
        super(parent, field, false, false, false, null);
        this.foreignTable = foreignTable;
        this.foreignKeyColumn = foreignKeyColumn;
    }

    @Override
    public boolean isNativelySupportedType() {
        return false;
    }

    public boolean isForeignKey() {
        return true;
    }

    public @NotNull TableArch getForeignTable() {
        return foreignTable;
    }

    @Override
    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow) {
        List<PrefixedColumn> fkColumns = List.of(foreignKeyColumn.prefixed(parent.sqlName()));
        return switch (follow) {
            case NO_FOLLOW -> fkColumns;
            case FOLLOW_ONE_LEVEL -> ListBuilder.concat(fkColumns, foreignTable.columns(NO_FOLLOW));
            case FOLLOW_ALL -> ListBuilder.concat(fkColumns, foreignTable.columns(FOLLOW_ALL));
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

    @Override
    public boolean isMultiColumn() {
        return false;
    }

    @Override
    public boolean isSingleColumn() {
        return true;
    }
}
