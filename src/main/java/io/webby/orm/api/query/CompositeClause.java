package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositeClause extends Unit implements Clause {
    private final Where where;
    private final OrderBy orderBy;
    private final LimitClause limit;
    private final Offset offset;

    public CompositeClause(@Nullable Where where,
                           @Nullable OrderBy orderBy,
                           @Nullable LimitClause limit,
                           @Nullable Offset offset) {
        super(joinLines(where, orderBy, limit, offset), flattenArgsOf(Arrays.asList(where, orderBy, limit, offset)));
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

    private static @NotNull String joinLines(@Nullable Clause @NotNull ... clauses) {
        return Stream.<Representable>of(clauses)
                .filter(Objects::nonNull)
                .map(Representable::repr)
                .collect(Collectors.joining("\n"));
    }
}
