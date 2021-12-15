package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.webby.orm.api.Engine;
import org.jetbrains.annotations.NotNull;

public class SelectWhereBuilder {
    private final String table;
    private final ImmutableList.Builder<Term> terms = ImmutableList.builder();
    private final ClauseBuilder clause = new ClauseBuilder();

    public SelectWhereBuilder(@NotNull String table) {
        this.table = table;
    }

    public @NotNull SelectWhereBuilder select(@NotNull Term term) {
        terms.add(term);
        return this;
    }

    public @NotNull SelectWhereBuilder select(@NotNull Term term1, @NotNull Term term2) {
        terms.add(term1, term2);
        return this;
    }

    public @NotNull SelectWhereBuilder select(@NotNull Term @NotNull ... terms) {
        this.terms.add(terms);
        return this;
    }

    public @NotNull SelectWhereBuilder select(@NotNull Iterable<Term> terms) {
        this.terms.addAll(terms);
        return this;
    }

    public @NotNull SelectWhereBuilder where(@NotNull Where where) {
        clause.with(where);
        return this;
    }

    public @NotNull SelectWhereBuilder with(@NotNull OrderBy orderBy) {
        clause.with(orderBy);
        return this;
    }

    public @NotNull SelectWhereBuilder with(@NotNull LimitClause limit) {
        clause.with(limit);
        return this;
    }

    public @NotNull SelectWhereBuilder with(@NotNull Offset offset) {
        clause.with(offset);
        return this;
    }

    public @NotNull SelectWhereBuilder with(@NotNull Pagination pagination, @NotNull Engine engine) {
        clause.with(pagination, engine);
        return this;
    }

    public @NotNull SelectWhere build() {
        return new SelectWhere(new SelectFrom(table, terms.build()), clause.build());
    }
}
