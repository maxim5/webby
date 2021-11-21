package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;

import static io.webby.util.sql.api.query.InvalidQueryException.assure;

public class FuncExpr extends SimpleRepr implements Term {
    private final Func func;

    public FuncExpr(@NotNull Func func, @NotNull Term term) {
        super("%s(%s)".formatted(func.repr(), term.repr()));
        this.func = func;
        assure(func.matchesInput(term), "Incompatible function `%s` input: `%s`", func, term);
    }

    public FuncExpr(@NotNull Func func, @NotNull Term term1, @NotNull Term term2) {
        super("%s(%s, %s)".formatted(func.repr(), term1.repr(), term2.repr()));
        this.func = func;
        assure(func.matchesInput(term1, term2), "Incompatible function `%s` input: [`%s`, `%s`]", func, term1, term2);
    }

    @Override
    public @NotNull TermType type() {
        return func.resultType();
    }
}
