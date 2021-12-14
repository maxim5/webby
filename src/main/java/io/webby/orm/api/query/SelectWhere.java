package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import static io.webby.orm.api.query.Units.flattenArgsOf;
import static io.webby.orm.api.query.Units.joinWithLines;

public class SelectWhere extends UnitLazy implements SelectQuery {
    private final SelectFrom selectFrom;
    private final CompositeClause clause;

    public SelectWhere(@NotNull SelectFrom selectFrom, @NotNull CompositeClause clause) {
        super(flattenArgsOf(selectFrom, clause));
        this.selectFrom = selectFrom;
        this.clause = clause;
    }

    public static @NotNull SelectWhere of(@NotNull SelectFrom selectFrom) {
        return new SelectWhere(selectFrom, new ClauseBuilder().build());
    }

    public static @NotNull SelectWhere of(@NotNull SelectFrom selectFrom, @NotNull Where where) {
        return new SelectWhere(selectFrom, new ClauseBuilder().with(where).build());
    }

    public static @NotNull SelectWhere of(@NotNull SelectFrom selectFrom, @NotNull Where where, @NotNull OrderBy orderBy) {
        return new SelectWhere(selectFrom, new ClauseBuilder().with(where).with(orderBy).build());
    }

    @Override
    public @NotNull SelectQuery withTable(@NotNull String tableName) {
        selectFrom.withTable(tableName);
        return this;
    }

    @Override
    protected @NotNull String supplyRepr() {
        return joinWithLines(selectFrom, clause);
    }
}
