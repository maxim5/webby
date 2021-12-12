package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class NumberLiteral extends Unit implements Term {
    public NumberLiteral(@NotNull Number num) {
        super(num.toString());
    }

    @Override
    public @NotNull TermType type() {
        return TermType.NUMBER;
    }
}
