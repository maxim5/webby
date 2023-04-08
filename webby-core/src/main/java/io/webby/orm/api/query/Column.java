package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a column mention in the SQL query. Is a {@link Term} and is {@link Named}. Does not have args.
 */
public interface Column extends Named {
    @Override
    default @NotNull String repr() {
        return name();
    }

    @Override
    default @NotNull Args args() {
        return Args.of();
    }

    /**
     * Wraps the column to make it distinct.
     */
    default @NotNull DistinctColumn distinct() {
        return new DistinctColumn(this);
    }
}
