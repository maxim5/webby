package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static io.webby.orm.api.query.Args.flattenArgsOf;
import static io.webby.orm.api.query.Representables.joinWithLines;

public class CompositeFilter extends Unit implements Filter {
    private final Where where;
    private final OrderBy orderBy;
    private final LimitClause limit;
    private final Offset offset;

    public CompositeFilter(@Nullable Where where,
                           @Nullable OrderBy orderBy,
                           @Nullable LimitClause limit,
                           @Nullable Offset offset) {
        super(joinWithLines(where, orderBy, limit, offset), flattenArgsOf(Arrays.asList(where, orderBy, limit, offset)));
        this.where = where;
        this.orderBy = orderBy;
        this.limit = limit;
        this.offset = offset;
    }

    public static @NotNull CompositeFilterBuilder builder() {
        return new CompositeFilterBuilder();
    }

    public @Nullable Where where() {
        return where;
    }

    public @Nullable OrderBy orderBy() {
        return orderBy;
    }

    public @Nullable LimitClause limit() {
        return limit;
    }

    public @Nullable Offset offset() {
        return offset;
    }
}
