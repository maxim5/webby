package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class ColumnTerm extends SimpleRepr implements Term {
    private final Column column;
    private final Term term;

    public ColumnTerm(@NotNull Column column, @NotNull Term term) {
        super(term.repr());
        this.column = column;
        this.term = term;
        assert TermType.match(column.type(), term.type());
    }

    public static @NotNull ColumnTerm ofLiteral(@NotNull Column column, @NotNull NumberLiteral literal) {
        return new ColumnTerm(column, literal);
    }

    public static @NotNull ColumnTerm ofLiteral(@NotNull Column column, @NotNull StringLiteral literal) {
        return new ColumnTerm(column, literal);
    }

    public @NotNull Column column() {
        return column;
    }

    public @NotNull Term term() {
        return term;
    }

    @Override
    public @NotNull TermType type() {
        return column.type();
    }
}
