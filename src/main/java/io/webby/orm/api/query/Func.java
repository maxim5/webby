package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum Func implements Representable {
    HEX("hex", List.of(TermType.STRING), TermType.STRING);

    private final String repr;
    private final List<TermType> inputTypes;
    private final TermType resultType;

    Func(@NotNull String repr, @NotNull List<TermType> inputTypes, @NotNull TermType resultType) {
        this.repr = repr;
        this.inputTypes = inputTypes;
        this.resultType = resultType;
    }

    @Override
    public @NotNull String repr() {
        return repr;
    }

    public int arity() {
        return inputTypes.size();
    }

    public boolean matchesInput(@NotNull Term term) {
        return arity() == 1 && TermType.match(inputTypes.get(0), term.type());
    }

    public boolean matchesInput(@NotNull Term term1, @NotNull Term term2) {
        return arity() == 2 && TermType.match(inputTypes.get(0), term1.type()) && TermType.match(inputTypes.get(1), term2.type());
    }

    public @NotNull TermType resultType() {
        return resultType;
    }

    public @NotNull FuncExpr of(@NotNull Term term) {
        return new FuncExpr(this, term);
    }

    public @NotNull FuncExpr of(@NotNull Term term1, @NotNull Term term2) {
        return new FuncExpr(this, term1, term2);
    }
}
