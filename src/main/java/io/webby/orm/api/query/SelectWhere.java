package io.webby.orm.api.query;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

import static io.webby.orm.api.query.Units.flattenArgsOf;
import static io.webby.orm.api.query.Units.joinWithLines;

public class SelectWhere extends Unit implements SelectQuery {
    private final SelectFrom selectFrom;
    private final CompositeClause clause;

    public SelectWhere(@NotNull SelectFrom selectFrom, @NotNull CompositeClause clause) {
        super(joinWithLines(selectFrom, clause), flattenArgsOf(selectFrom, clause));
        this.selectFrom = selectFrom;
        this.clause = clause;
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
        return selectFrom.termsNumber();
    }
}
