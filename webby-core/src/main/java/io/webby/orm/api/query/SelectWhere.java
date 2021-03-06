package io.webby.orm.api.query;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

import static io.webby.orm.api.query.Args.flattenArgsOf;
import static io.webby.orm.api.query.Representables.joinWithLines;

public class SelectWhere extends Unit implements SelectQuery {
    private final int columnsNumber;

    public SelectWhere(@NotNull SelectFrom selectFrom, @NotNull CompositeFilter clause) {
        super(joinWithLines(selectFrom, clause), flattenArgsOf(selectFrom, clause));
        columnsNumber = selectFrom.termsNumber();
    }

    public static @NotNull SelectWhereBuilder from(@NotNull String table) {
        return new SelectWhereBuilder(table);
    }

    public static @NotNull SelectWhereBuilder from(@NotNull TableMeta meta) {
        return from(meta.sqlTableName());
    }

    public static @NotNull SelectWhereBuilder from(@NotNull BaseTable<?> table) {
        return from(table.meta());
    }

    @Override
    public int columnsNumber() {
        return columnsNumber;
    }
}
