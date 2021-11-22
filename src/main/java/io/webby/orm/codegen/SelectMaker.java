package io.webby.orm.codegen;

import io.webby.orm.api.ReadFollow;
import io.webby.orm.arch.ForeignTableField;
import io.webby.orm.arch.OneColumnTableField;
import io.webby.orm.arch.PrefixedColumn;
import io.webby.orm.arch.TableArch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.function.Function;

class SelectMaker {
    private final TableArch table;

    public SelectMaker(@NotNull TableArch table) {
        this.table = table;
    }

    public @NotNull Snippet make(@NotNull ReadFollow follow) {
        Function<PrefixedColumn, String> toSqlName =
                follow == ReadFollow.NO_FOLLOW ?
                        PrefixedColumn::sqlName :
                        PrefixedColumn::sqlPrefixedName;

        String sqlName = table.sqlName();
        List<String> columns = table.columns(follow).stream().map(toSqlName).toList();
        List<LeftJoin> joins = table.foreignFields(follow).stream().map(LeftJoin::from).toList();
        return compose(sqlName, columns, joins);
    }

    @VisibleForTesting
    static @NotNull Snippet compose(@NotNull String table, @NotNull List<String> columns, @NotNull List<LeftJoin> joins) {
        return new Snippet()
                .withFormattedLine("SELECT %s", String.join(", ", columns))
                .withFormattedLine("FROM %s", table)
                .withLines(joins.stream().map(join -> "LEFT JOIN %s ON %s".formatted(join.table, join.on)));
    }

    @VisibleForTesting
    record LeftJoin(@NotNull String table, @NotNull String on) {
        public static @NotNull LeftJoin from(@NotNull ForeignTableField field) {
            TableArch foreignTable = field.getForeignTable();
            String on = "%s.%s = %s.%s".formatted(field.parent().sqlName(),
                                                  field.foreignKeyColumn().sqlName(),
                                                  foreignTable.sqlName(),
                                                  ((OneColumnTableField) foreignTable.primaryKeyField()).column().sqlName());
            return new LeftJoin(foreignTable.sqlName(), on);
        }
    }
}
