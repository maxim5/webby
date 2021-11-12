package io.webby.util.sql.codegen;

import io.webby.util.sql.api.FollowReferences;
import io.webby.util.sql.schema.ForeignTableField;
import io.webby.util.sql.schema.OneColumnTableField;
import io.webby.util.sql.schema.PrefixedColumn;
import io.webby.util.sql.schema.TableSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.function.Function;

class SelectMaker {
    private final TableSchema table;

    public SelectMaker(@NotNull TableSchema table) {
        this.table = table;
    }

    public @NotNull Snippet make(@NotNull FollowReferences follow) {
        Function<PrefixedColumn, String> toSqlName =
                follow == FollowReferences.NO_FOLLOW ?
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
                .withFormattedLine("SELECT %s FROM %s", String.join(", ", columns), table)
                .withLines(joins.stream().map(join -> "LEFT JOIN %s ON %s".formatted(join.table, join.on)).toList());
    }

    @VisibleForTesting
    record LeftJoin(@NotNull String table, @NotNull String on) {
        public static @NotNull LeftJoin from(@NotNull ForeignTableField field) {
            TableSchema foreignTable = field.getForeignTable();
            String on = "%s.%s = %s.%s".formatted(field.parent().sqlName(),
                                                  field.foreignKeyColumn().sqlName(),
                                                  foreignTable.sqlName(),
                                                  ((OneColumnTableField) foreignTable.primaryKeyField()).column().sqlName());
            return new LeftJoin(foreignTable.sqlName(), on);
        }
    }
}
