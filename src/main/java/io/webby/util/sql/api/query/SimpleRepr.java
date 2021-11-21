package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;

public abstract class SimpleRepr implements Repr {
    private final String repr;

    public SimpleRepr(@NotNull String repr) {
        this.repr = repr;
    }

    @Override
    public @NotNull String repr() {
        return repr;
    }

    @Override
    public String toString() {
        return repr;
    }
}
