package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.orm.api.query.Units.flattenArgsOf;
import static io.webby.orm.api.query.Units.joinWithLines;

public class SelectWhere extends UnitLazy implements SelectQuery {
    private final SelectFrom selectFrom;
    private final Where where;
    private final OrderBy orderBy;

    public SelectWhere(@NotNull SelectFrom selectFrom, @Nullable Where where, @Nullable OrderBy orderBy) {
        super(flattenArgsOf(selectFrom, where, orderBy));
        this.selectFrom = selectFrom;
        this.where = where;
        this.orderBy = orderBy;
    }

    public static @NotNull SelectWhere of(@NotNull SelectFrom selectFrom) {
        return new SelectWhere(selectFrom, null, null);
    }

    public static @NotNull SelectWhere of(@NotNull SelectFrom selectFrom, @NotNull Where where) {
        return new SelectWhere(selectFrom, where, null);
    }

    public static @NotNull SelectWhere of(@NotNull SelectFrom selectFrom, @NotNull Where where, @NotNull OrderBy orderBy) {
        return new SelectWhere(selectFrom, where, orderBy);
    }

    @Override
    public @NotNull SelectQuery withTable(@NotNull String tableName) {
        selectFrom.withTable(tableName);
        return this;
    }

    @Override
    protected @NotNull String supplyRepr() {
        return joinWithLines(selectFrom, where, orderBy);
    }
}
