package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class StringLiteral extends Unit implements Term {
    public StringLiteral(@NotNull String literal) {
        super("'%s'".formatted(literal));
    }

    @Override
    public @NotNull TermType type() {
        return TermType.STRING;
    }
}
