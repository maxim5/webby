package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public interface Column extends Named {
    @Override
    default @NotNull String repr() {
        return name();
    }

    @Override
    default @NotNull Args args() {
        return Args.of();
    }

    default @NotNull DistinctColumn distinct() {
        return new DistinctColumn(this);
    }
}
