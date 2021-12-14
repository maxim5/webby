package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Column extends Term {
    @NotNull String name();

    @Override
    default @NotNull String repr() {
        return name();
    }

    @Override
    default @NotNull List<Object> args() {
        return List.of();
    }

    default @NotNull DistinctColumn distinct() {
        return new DistinctColumn(this);
    }
}
