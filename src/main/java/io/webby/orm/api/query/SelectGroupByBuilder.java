package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SelectGroupByBuilder {
    private final String table;
    private FuncExpr funcExpr = null;
    private final ImmutableList.Builder<Named> terms = ImmutableList.builder();
    private final ClauseBuilder clause = new ClauseBuilder();

    public SelectGroupByBuilder(@NotNull String table) {
        this.table = table;
    }

    public @NotNull SelectGroupByBuilder select(@NotNull List<Named> terms, @NotNull FuncExpr aggregate) {
        return groupBy(terms).aggregate(aggregate);
    }

    public @NotNull SelectGroupByBuilder aggregate(@NotNull FuncExpr aggregate) {
        assert funcExpr == null : "Aggregate function already set: %s".formatted(funcExpr);
        assert aggregate.isAggregate() : "Non-aggregate function supplied: %s".formatted(aggregate);
        funcExpr = aggregate;
        return this;
    }

    public @NotNull SelectGroupByBuilder groupBy(@NotNull Named term) {
        terms.add(term);
        return this;
    }

    public @NotNull SelectGroupByBuilder groupBy(@NotNull Named term1, @NotNull Named term2) {
        terms.add(term1, term2);
        return this;
    }

    public @NotNull SelectGroupByBuilder groupBy(@NotNull Named @NotNull ... terms) {
        this.terms.add(terms);
        return this;
    }

    public @NotNull SelectGroupByBuilder groupBy(@NotNull Iterable<Named> terms) {
        this.terms.addAll(terms);
        return this;
    }

    public @NotNull SelectGroupByBuilder with(@NotNull Where where) {
        clause.with(where);
        return this;
    }

    public @NotNull SelectGroupByBuilder with(@NotNull OrderBy orderBy) {
        clause.with(orderBy);
        return this;
    }

    public @NotNull SelectGroupBy build() {
        ImmutableList<Named> groupByTerms = terms.build();
        ImmutableList<Term> allTerms = ImmutableList.<Term>builder().addAll(groupByTerms).add(funcExpr).build();
        CompositeClause composite = clause.build();
        return new SelectGroupBy(new SelectFrom(table, allTerms), composite.where(), new GroupBy(groupByTerms), composite.orderBy());
    }
}
