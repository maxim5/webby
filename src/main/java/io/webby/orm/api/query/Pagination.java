package io.webby.orm.api.query;

import io.webby.orm.api.PageToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Pagination(@Nullable ColumnTerm lastItem, int offset, int limit) {
    public static final int NO_OFFSET = -1;

    public Pagination {
        assert offset > 0 || offset == NO_OFFSET : "Invalid offset: " + offset;
        assert lastItem != null || offset > 0 || limit > 0 : "One of `lastItem`, `offset` or `limit` must be set";
    }

    public static @NotNull Pagination of(int offset, int limit) {
        return new Pagination(null, offset, limit);
    }

    public static @NotNull Pagination of(@NotNull ColumnTerm lastItem, int limit) {
        return new Pagination(lastItem, NO_OFFSET, limit);
    }

    public static @NotNull Pagination of(@NotNull PageToken token, @NotNull Column column, int limit) {
        if (token.hasLastItem()) {
            ColumnTerm columnTerm = new ColumnTerm(column, new Variable(token.lastItem(), TermType.WILDCARD));
            return of(columnTerm, limit);
        }
        if (token.hasOffset()) {
            return of(token.offset(), limit);
        }
        throw new IllegalArgumentException("Invalid token: " + token);
    }

    public boolean hasLastItem() {
        return lastItem != null;
    }

    public boolean hasOffset() {
        return offset != NO_OFFSET;
    }
}
