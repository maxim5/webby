package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;

public interface Column extends Term {
    @NotNull String name();

    @Override
    default @NotNull String repr() {
        return name();
    }
}
