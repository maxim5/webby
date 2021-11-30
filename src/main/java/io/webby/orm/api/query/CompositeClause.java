package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositeClause extends SimpleRepr implements Clause {
    private final Where where;
    private final OrderBy orderBy;
    private final Offset offset;
    private final Limit limit;

    public CompositeClause(@Nullable Where where,
                           @Nullable OrderBy orderBy,
                           @Nullable Offset offset,
                           @Nullable Limit limit) {
        super(combine(where, orderBy, offset, limit));
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

    public @Nullable Limit limit() {
        return limit;
    }

    private static @NotNull String combine(@Nullable Clause @NotNull ... clauses) {
        return Stream.<Repr>of(clauses)
                .filter(Objects::nonNull)
                .map(Repr::repr)
                .collect(Collectors.joining("\n"));
    }
}
