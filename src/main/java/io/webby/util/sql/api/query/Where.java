package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Where extends SimpleRepr implements Repr {
    public Where(@NotNull BoolTerm term) {
        super("WHERE %s".formatted(term.repr()));
    }

    public static @NotNull Where of(@NotNull BoolTerm term) {
        return new Where(term);
    }

    public static @NotNull Where and(@NotNull BoolTerm term1, @NotNull BoolTerm term2) {
        return new Where(new BoolOp(List.of(term1, term2), BoolOpType.AND));
    }

    public static @NotNull Where and(@NotNull BoolTerm ... terms) {
        return new Where(new BoolOp(List.of(terms), BoolOpType.AND));
    }

    public static @NotNull Where or(@NotNull BoolTerm term1, @NotNull BoolTerm term2) {
        return new Where(new BoolOp(List.of(term1, term2), BoolOpType.AND));
    }

    public static @NotNull Where or(@NotNull BoolTerm ... terms) {
        return new Where(new BoolOp(List.of(terms), BoolOpType.OR));
    }

    public static @NotNull Where hardcoded(@NotNull String raw) {
        return new Where(new HardcodedBoolTerm(raw));
    }
}
