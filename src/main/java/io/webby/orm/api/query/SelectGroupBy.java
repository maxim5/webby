package io.webby.orm.api.query;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.orm.api.query.Units.flattenArgsOf;
import static io.webby.orm.api.query.Units.joinWithLines;

public class SelectGroupBy extends Unit implements SelectQuery {
    private final int columnsNumber;

    public SelectGroupBy(@NotNull SelectFrom selectFrom, @Nullable Where where,
                         @NotNull GroupBy groupBy, @Nullable Having having, @Nullable OrderBy orderBy) {
        super(joinWithLines(selectFrom, where, groupBy, having, orderBy),
              flattenArgsOf(selectFrom, where, groupBy, having, orderBy));
        columnsNumber = selectFrom.termsNumber();
    }

    public static @NotNull SelectGroupByBuilder from(@NotNull String table) {
        return new SelectGroupByBuilder(table);
    }

    public static @NotNull SelectGroupByBuilder from(@NotNull TableMeta meta) {
        return from(meta.sqlTableName());
    }

    public static @NotNull SelectGroupByBuilder from(@NotNull BaseTable<?> table) {
        return from(table.meta());
    }

    @Override
    public int columnsNumber() {
        return columnsNumber;
    }
}
