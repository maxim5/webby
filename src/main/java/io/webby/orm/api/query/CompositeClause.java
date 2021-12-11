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
    private final Offset offset;
    private final LimitClause limit;

    public CompositeClause(@Nullable Where where,
                           @Nullable OrderBy orderBy,
                           @Nullable Offset offset,
                           @Nullable LimitClause limit) {
        super(joinLines(where, orderBy, offset, limit), flattenArgsOf(Arrays.asList(where, orderBy, offset, limit)));
        this.where = where;
        this.orderBy = orderBy;
        this.offset = offset;
        this.limit = limit;
    }

    public @Nullable Where where() {
        return where;
    }

    public @Nullable OrderBy orderBy() {
        return orderBy;
    }

    public @Nullable Offset offset() {
        return offset;
    }

    public @Nullable LimitClause limit() {
        return limit;
    }

    private static @NotNull String joinLines(@Nullable Clause @NotNull ... clauses) {
        return Stream.<Representable>of(clauses)
                .filter(Objects::nonNull)
                .map(Representable::repr)
                .collect(Collectors.joining("\n"));
    }
}
