package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.List;

public class Shortcuts {
    public static final Term NULL = new HardcodedTerm("NULL", TermType.WILDCARD);
    public static final Term STAR = new HardcodedTerm("*", TermType.WILDCARD);

    public static final BoolTerm TRUE = new HardcodedBoolTerm("TRUE");
    public static final BoolTerm FALSE = new HardcodedBoolTerm("FALSE");

    public static final Term ZERO = num(0);
    public static final Term ONE = num(1);

    public static @NotNull NumberLiteral num(@NotNull Number value) {
        return new NumberLiteral(value);
    }

    public static @NotNull StringLiteral literal(@NotNull String value) {
        return new StringLiteral(value);
    }

    public static @NotNull Variable var(@NotNull Number value) {
        return new Variable(value, TermType.NUMBER);
    }

    public static @NotNull Variable var(@NotNull String value) {
        return new Variable(value, TermType.STRING);
    }

    public static @NotNull Variable var(byte @NotNull[] value) {
        return new Variable(value, TermType.STRING);
    }

    public static @NotNull Variable var(@NotNull Timestamp timestamp) {
        return new Variable(timestamp, TermType.TIME);
    }

    public static @NotNull BoolTerm lookupBy(@NotNull Column column, @NotNull Number variable) {
        return CompareType.EQ.compare(column, var(variable));
    }

    public static @NotNull BoolTerm lookupBy(@NotNull Column column, @NotNull String variable) {
        return CompareType.EQ.compare(column, var(variable));
    }

    public static @NotNull IsIn isIn(@NotNull Term lhs, @NotNull Term @NotNull ... terms) {
        return new IsIn(lhs, List.of(terms));
    }

    public static @NotNull IsIn isIn(@NotNull Term lhs, @NotNull List<Term> terms) {
        return new IsIn(lhs, terms);
    }

    public static @NotNull Like like(@NotNull Term lhs, @NotNull Term rhs) {
        return new Like(lhs, rhs);
    }

    public static @NotNull Between between(@NotNull Term term, @NotNull Term left, @NotNull Term right) {
        return new Between(term, left, right);
    }
}
