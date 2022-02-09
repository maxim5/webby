package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class NumberLiteral extends Unit implements Term {
    public NumberLiteral(@NotNull Number num) {
        super(num.toString());
    }

    public NumberLiteral(int num) {
        super(String.valueOf(num));
    }

    public NumberLiteral(long num) {
        super(String.valueOf(num));
    }

    @Override
    public @NotNull TermType type() {
        return TermType.NUMBER;
    }
}
