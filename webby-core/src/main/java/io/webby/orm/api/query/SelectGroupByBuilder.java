package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class SelectGroupByBuilder {
    private final String table;
    private FuncExpr funcExpr = null;
    private final ImmutableList.Builder<Named> terms = ImmutableList.builder();
    private Having having;
    private final CompositeFilterBuilder filter = new CompositeFilterBuilder();

    public SelectGroupByBuilder(@NotNull String table) {
        this.table = table;
    }

    public @NotNull SelectGroupByBuilder select(@NotNull Named term, @NotNull FuncExpr aggregate) {
        return groupBy(term).aggregate(aggregate);
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

    public @NotNull SelectGroupByBuilder having(@NotNull Having having) {
        this.having = having;
        return this;
    }

    public @NotNull SelectGroupByBuilder where(@NotNull Where where) {
        filter.with(where);
        return this;
    }

    public @NotNull SelectGroupByBuilder orderBy(@NotNull OrderBy orderBy) {
        filter.with(orderBy);
        return this;
    }

    public @NotNull SelectGroupByBuilder applying(@NotNull Consumer<SelectGroupByBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    public @NotNull SelectGroupBy build() {
        ImmutableList<Named> groupByTerms = terms.build();
        ImmutableList<Term> allTerms = ImmutableList.<Term>builder().addAll(groupByTerms).add(funcExpr).build();
        CompositeFilter composite = filter.build();
        return new SelectGroupBy(new SelectFrom(table, allTerms), composite.where(),
                                 new GroupBy(groupByTerms), having, composite.orderBy());
    }
}
