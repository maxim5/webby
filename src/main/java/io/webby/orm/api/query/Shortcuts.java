package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class Shortcuts {
    public static @NotNull NumberLiteral num(@NotNull Number number) {
        return new NumberLiteral(number);
    }

    public static @NotNull StringLiteral literal(@NotNull String literal) {
        return new StringLiteral(literal);
    }

    public static @NotNull Like like(@NotNull Term lhs, @NotNull Term rhs) {
        return new Like(lhs, rhs);
    }
}
