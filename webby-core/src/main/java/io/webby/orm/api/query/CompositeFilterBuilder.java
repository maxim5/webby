package io.webby.orm.api.query;

import io.webby.orm.api.Engine;
import org.jetbrains.annotations.NotNull;

import static io.webby.orm.api.query.CompareType.GT;
import static io.webby.orm.api.query.CompareType.LT;
import static java.util.Objects.requireNonNull;

public class CompositeFilterBuilder {
    private Where where;
    private OrderBy orderBy;
    private LimitClause limit;
    private Offset offset;

    public @NotNull CompositeFilterBuilder with(@NotNull Where where) {
        this.where = this.where == null ? where : this.where.andTerm(where.term());
        return this;
    }

    public @NotNull CompositeFilterBuilder with(@NotNull OrderBy orderBy) {
        this.orderBy = this.orderBy == null ? orderBy : this.orderBy.withMoreTerms(orderBy.terms());
        return this;
    }

    public @NotNull CompositeFilterBuilder with(@NotNull LimitClause limit) {
        assert this.limit == null : "Duplicate limit specified: existing: %s, updated: %s".formatted(this.limit, limit);
        this.limit = limit;
        return this;
    }

    public @NotNull CompositeFilterBuilder with(@NotNull Offset offset) {
        assert this.offset == null : "Duplicate offset specified: existing: %s, updated: %s".formatted(this.offset, offset);
        this.offset = offset;
        return this;
    }

    public @NotNull CompositeFilterBuilder with(@NotNull Pagination pagination, @NotNull Engine engine) {
        if (pagination.hasLastItem()) {
            ColumnTerm lastItem = requireNonNull(pagination.lastItem());
            Order order = requireNonNull(pagination.order());
            with(Where.of((order == Order.ASC ? GT : LT).compare(lastItem.column(), lastItem.term())));
        }
        if (pagination.hasOffset()) {
            with(Offset.of(pagination.offset()));
        }
        switch (engine) {
            case H2, MySQL, PostgreSQL, SQLite -> with(Limit.of(pagination.limit()));
            case MsSqlServer, Oracle -> with(FetchOnly.of(pagination.limit()));
        }
        return this;
    }

    public @NotNull CompositeFilter build() {
        return new CompositeFilter(where, orderBy, limit, offset);
    }
}
