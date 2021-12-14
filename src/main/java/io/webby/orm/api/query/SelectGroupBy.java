package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.webby.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.webby.orm.api.query.Units.flattenArgsOf;
import static io.webby.orm.api.query.Units.joinWithLines;

public class SelectGroupBy extends UnitLazy implements SelectQuery {
    private final ImmutableList<Named> terms;
    private final FuncExpr aggregate;

    private final SelectFrom selectFrom;
    private final GroupBy groupBy;
    private final Where where;
    private final OrderBy orderBy;

    public SelectGroupBy(@NotNull List<Named> terms, @NotNull FuncExpr aggregate,
                         @Nullable Where where, @Nullable OrderBy orderBy) {
        super(EasyIterables.concat(flattenArgsOf(terms), flattenArgsOf(aggregate, where, orderBy)));
        assert aggregate.isAggregate() : "Non-aggregate function supplied: %s".formatted(aggregate);
        this.terms = ImmutableList.copyOf(terms);
        this.aggregate = aggregate;
        this.selectFrom = new SelectFrom(ImmutableList.<Term>builder().addAll(terms).add(aggregate).build());
        this.groupBy = new GroupBy(terms);
        this.where = where;
        this.orderBy = orderBy;
    }

    public SelectGroupBy(@NotNull List<Named> terms, @NotNull FuncExpr aggregate) {
        this(terms, aggregate, null, null);
    }

    public static @NotNull SelectGroupBy of(@NotNull Named term, @NotNull FuncExpr aggregate) {
        return new SelectGroupBy(List.of(term), aggregate);
    }

    public static @NotNull SelectGroupBy of(@NotNull Named term1, @NotNull Named term2, @NotNull FuncExpr aggregate) {
        return new SelectGroupBy(List.of(term1, term2), aggregate);
    }

    @Override
    public @NotNull SelectGroupBy withTable(@NotNull String tableName) {
        selectFrom.withTable(tableName);
        return this;
    }

    @Override
    protected @NotNull String supplyRepr() {
        return joinWithLines(selectFrom, where, groupBy, orderBy);
    }

    public @NotNull ImmutableList<Named> terms() {
        return terms;
    }

    public @NotNull FuncExpr aggregate() {
        return aggregate;
    }
}
