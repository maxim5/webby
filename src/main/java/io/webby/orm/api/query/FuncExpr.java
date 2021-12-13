package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FuncExpr extends Unit implements Term {
    private final Func func;

    public FuncExpr(@NotNull Func func, @NotNull Term term) {
        super(func.format(term), term.args());
        this.func = func;
    }

    public FuncExpr(@NotNull Func func, @NotNull Term term1, @NotNull Term term2) {
        super(func.format(term1, term2), flattenArgsOf(term1, term2));
        this.func = func;
    }

    public FuncExpr(@NotNull Func func, @NotNull List<Term> terms) {
        super(func.format(terms), flattenArgsOf(terms));
        this.func = func;
    }

    @Override
    public @NotNull TermType type() {
        return func.resultType();
    }
}
