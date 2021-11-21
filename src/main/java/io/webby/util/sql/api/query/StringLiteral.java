package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;

public class StringLiteral extends SimpleRepr implements Term {
    public StringLiteral(@NotNull String literal) {
        super("'%s'".formatted(literal));
    }

    @Override
    public @NotNull TermType type() {
        return TermType.STRING;
    }
}
