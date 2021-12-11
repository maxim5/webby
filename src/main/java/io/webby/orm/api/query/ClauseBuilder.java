package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import static io.webby.orm.api.query.CompareType.GT;
import static java.util.Objects.requireNonNull;

public class ClauseBuilder {
    private Where where;
    private OrderBy orderBy;
    private Offset offset;
    private LimitClause limit;

    public @NotNull ClauseBuilder with(@NotNull Where where) {
        this.where = this.where == null ? where : this.where.andTerm(where.term());
        return this;
    }

    public @NotNull ClauseBuilder with(@NotNull OrderBy orderBy) {
        this.orderBy = this.orderBy == null ? orderBy : this.orderBy.withMoreTerms(orderBy.terms());
        return this;
    }

    public @NotNull ClauseBuilder with(@NotNull Offset offset) {
        assert this.offset == null : "Duplicate offset specified: existing: %s, updated: %s".formatted(this.offset, offset);
        this.offset = offset;
        return this;
    }

    public @NotNull ClauseBuilder with(@NotNull LimitClause limit) {
        assert this.limit == null : "Duplicate limit specified: existing: %s, updated: %s".formatted(this.limit, limit);
        this.limit = limit;
        return this;
    }

    public @NotNull ClauseBuilder with(@NotNull Pagination pagination) {
        if (pagination.hasLastItem()) {
            ColumnTerm lastItem = requireNonNull(pagination.lastItem());
            with(Where.of(GT.compare(lastItem.column(), lastItem.term())));
        }
        if (pagination.hasOffset()) {
            with(Offset.of(pagination.offset()));
        }
        return with(Limit.of(pagination.limit()));
    }

    public @NotNull CompositeClause build() {
        return new CompositeClause(where, orderBy, offset, limit);
    }
}
