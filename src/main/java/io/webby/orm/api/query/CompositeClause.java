package io.webby.orm.api.query;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static io.webby.orm.api.query.Units.flattenArgsOf;
import static io.webby.orm.api.query.Units.joinWithLines;

public class CompositeClause extends Unit implements Clause {
    private final Where where;
    private final OrderBy orderBy;
    private final LimitClause limit;
    private final Offset offset;

    public CompositeClause(@Nullable Where where,
                           @Nullable OrderBy orderBy,
                           @Nullable LimitClause limit,
                           @Nullable Offset offset) {
        super(joinWithLines(where, orderBy, limit, offset), flattenArgsOf(Arrays.asList(where, orderBy, limit, offset)));
        this.where = where;
        this.orderBy = orderBy;
        this.limit = limit;
        this.offset = offset;
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
