package io.spbx.orm.arch.model;

import com.google.errorprone.annotations.Immutable;
import io.spbx.orm.api.ReadFollow;
import io.spbx.util.collect.ListBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.spbx.orm.api.ReadFollow.FOLLOW_ALL;
import static io.spbx.orm.api.ReadFollow.NO_FOLLOW;
import static io.spbx.util.base.EasyExceptions.newInternalError;

@Immutable
public class ForeignTableField extends TableField {
    private final TableArch foreignTable;
    private final Column foreignKeyColumn;

    public ForeignTableField(@NotNull TableArch parent,
                             @NotNull ModelField field,
                             boolean nullable,
                             @NotNull Defaults defaults,
                             @NotNull TableArch foreignTable,
                             @NotNull Column foreignKeyColumn) {
        super(parent, field, false, false, nullable, defaults, null, null);
        assert defaults.size() == 1 : "Columns of `%s` don't match the defaults: column=%s, defaults=%s"
            .formatted(field.name(), foreignKeyColumn, defaults);
        this.foreignTable = foreignTable;
        this.foreignKeyColumn = foreignKeyColumn;
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

    public @NotNull OneColumnTableField primaryKeyFieldInForeignTable() {
        TableField primaryKeyField = foreignTable.primaryKeyField();
        assert primaryKeyField instanceof OneColumnTableField :
            newInternalError("Primary key in foreign table does not match the foreign key: %s", this);
        return (OneColumnTableField) primaryKeyField;
    }

    public @NotNull Column primaryKeyColumnInForeignTable() {
        return primaryKeyFieldInForeignTable().column();
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
