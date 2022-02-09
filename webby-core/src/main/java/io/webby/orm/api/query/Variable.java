package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Variable extends Unit implements Term {
    private final TermType type;

    public Variable(@Nullable Object value, @NotNull TermType type) {
        super("?", Args.of(value));
        this.type = type;
    }

    public Variable(int value) {
        super("?", Args.of(value));
        this.type = TermType.NUMBER;
    }

    public Variable(long value) {
        super("?", Args.of(value));
        this.type = TermType.NUMBER;
    }

    @Override
    public @NotNull TermType type() {
        return type;
    }
}
