package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class Variable extends Unit implements Term {
    private final TermType type;

    public Variable(@Nullable Object value, @NotNull TermType type) {
        super("?", Collections.singletonList(value));
        this.type = type;
    }

    @Override
    public @NotNull TermType type() {
        return type;
    }
}
