package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class Shortcuts {
    public static final Term NULL = new HardcodedTerm("NULL", TermType.WILDCARD);
    public static final Term STAR = new HardcodedTerm("*", TermType.WILDCARD);
    public static final Term ZERO = num(0);
    public static final Term ONE = num(1);

    public static @NotNull NumberLiteral num(@NotNull Number number) {
        return new NumberLiteral(number);
    }

    public static @NotNull StringLiteral literal(@NotNull String literal) {
        return new StringLiteral(literal);
    }

    public static @NotNull Variable var(@NotNull String value) {
        return new Variable(value, TermType.STRING);
    }

    public static @NotNull Variable var(byte @NotNull[] value) {
        return new Variable(value, TermType.STRING);
    }

    public static @NotNull Like like(@NotNull Term lhs, @NotNull Term rhs) {
        return new Like(lhs, rhs);
    }
}
