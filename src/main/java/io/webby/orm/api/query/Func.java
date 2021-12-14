package io.webby.orm.api.query;

import com.google.mu.util.stream.BiStream;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.webby.orm.api.query.InvalidQueryException.assure;
import static io.webby.orm.api.query.TermType.*;

public enum Func implements Representable {
    COUNT("count", List.of(WILDCARD), NUMBER),
    SUM("sum", List.of(NUMBER), NUMBER),
    AVG("avg", List.of(NUMBER), NUMBER),
    MAX("max", List.of(NUMBER), NUMBER),
    MIN("min", List.of(NUMBER), NUMBER),

    FIRST("first", List.of(WILDCARD), WILDCARD),
    FIRST_NUM("first", List.of(NUMBER), NUMBER),
    FIRST_STR("first", List.of(STRING), STRING),

    LAST("last", List.of(WILDCARD), WILDCARD),
    LAST_NUM("last", List.of(NUMBER), NUMBER),
    LAST_STR("last", List.of(STRING), STRING),

    ABS("abs", List.of(NUMBER), NUMBER),
    SQUARE("square", List.of(NUMBER), NUMBER),
    SQRT("sqrt", List.of(NUMBER), NUMBER),

    LOWER("lower", List.of(STRING), STRING),
    LCASE("lcase", List.of(STRING), STRING),
    UPPER("upper", List.of(STRING), STRING),
    UCASE("ucase", List.of(STRING), STRING),

    TRIM("trim", List.of(STRING), STRING),
    LTRIM("ltrim", List.of(STRING), STRING),
    RTRIM("rtrim", List.of(STRING), STRING),

    ASCII("ascii", List.of(STRING), NUMBER),
    CHAR("char", List.of(NUMBER), STRING),
    STR("str", List.of(NUMBER), STRING),
    LEN("len", List.of(STRING), NUMBER),
    HEX("hex", List.of(STRING), STRING),
    SUBSTRING("substring", List.of(STRING, NUMBER, NUMBER), STRING),
    TRANSLATE("translate", List.of(STRING, STRING, STRING), STRING),

    CAST_AS("CAST", "CAST(%s AS %s)", List.of(WILDCARD, STRING), WILDCARD),
    CAST_AS_INT("CAST", "CAST(%s AS INT)", List.of(WILDCARD), NUMBER),
    CAST_AS_STR("CAST", "CAST(%s AS VARCHAR)", List.of(WILDCARD), STRING),

    CONCAT("concat", List.of(STRING, STRING), STRING),
    CONCAT3("concat", List.of(STRING, STRING, STRING), STRING),
    CONCAT4("concat", List.of(STRING, STRING, STRING, STRING), STRING),
    CONCAT5("concat", List.of(STRING, STRING, STRING, STRING, STRING), STRING),

    CONCAT_WS("concat_ws", List.of(STRING, STRING, STRING), STRING),
    CONCAT_WS3("concat_ws", List.of(STRING, STRING, STRING, STRING), STRING),
    CONCAT_WS4("concat_ws", List.of(STRING, STRING, STRING, STRING, STRING), STRING),
    CONCAT_WS5("concat_ws", List.of(STRING, STRING, STRING, STRING, STRING, STRING), STRING),

    COALESCE("coalesce", List.of(WILDCARD, WILDCARD), WILDCARD),
    COALESCE3("coalesce", List.of(WILDCARD, WILDCARD, WILDCARD), WILDCARD),
    COALESCE4("coalesce", List.of(WILDCARD, WILDCARD, WILDCARD, WILDCARD), WILDCARD),
    COALESCE5("coalesce", List.of(WILDCARD, WILDCARD, WILDCARD, WILDCARD, WILDCARD), WILDCARD);

    private final String repr;
    private final String pattern;
    private final List<TermType> inputTypes;
    private final TermType resultType;

    Func(@NotNull String repr, @NotNull String pattern, @NotNull List<TermType> inputTypes, @NotNull TermType resultType) {
        this.repr = repr;
        this.pattern = pattern;
        this.inputTypes = inputTypes;
        this.resultType = resultType;
    }

    Func(@NotNull String repr, @NotNull List<TermType> inputTypes, @NotNull TermType resultType) {
        this(repr, defaultFuncPattern(repr, inputTypes.size()), inputTypes, resultType);
    }

    @Override
    public @NotNull String repr() {
        return repr;
    }

    public int arity() {
        return inputTypes.size();
    }

    public boolean isAggregate() {
        return AGGREGATE_VALUES.contains(this);
    }

    public @NotNull String format(@NotNull Term term) {
        assure(matchesInput(term), "Incompatible function `%s` input: `%s`", this, term);
        return pattern.formatted(term.repr());
    }

    public @NotNull String format(@NotNull Term term1, @NotNull Term term2) {
        assure(matchesInput(term1, term2), "Incompatible function `%s` inputs: [`%s`, `%s`]", this, term1, term2);
        return pattern.formatted(term1.repr(), term2.repr());
    }

    public @NotNull String format(@NotNull List<Term> terms) {
        assure(matchesInput(terms), "Incompatible function `%s` inputs: `%s`", this, terms);
        return pattern.formatted(terms.stream().map(Representable::repr).toArray());
    }

    public boolean matchesInput(@NotNull Term term) {
        return arity() == 1 && match(inputTypes.get(0), term.type());
    }

    public boolean matchesInput(@NotNull Term term1, @NotNull Term term2) {
        return arity() == 2 && match(inputTypes.get(0), term1.type()) && match(inputTypes.get(1), term2.type());
    }

    public boolean matchesInput(@NotNull List<Term> terms) {
        return BiStream.zip(inputTypes, terms).allMatch(((inputType, term) -> match(inputType, term.type())));
    }

    public @NotNull TermType resultType() {
        return resultType;
    }

    public @NotNull FuncExpr apply(@NotNull Term term) {
        return new FuncExpr(this, term);
    }

    public @NotNull FuncExpr apply(@NotNull Term term1, @NotNull Term term2) {
        return new FuncExpr(this, term1, term2);
    }

    public @NotNull FuncExpr apply(@NotNull Term ... terms) {
        return apply(List.of(terms));
    }

    public @NotNull FuncExpr apply(@NotNull List<Term> terms) {
        return new FuncExpr(this, terms);
    }

    private static @NotNull String defaultFuncPattern(@NotNull String func, int arity) {
        return func + IntStream.range(0, arity).mapToObj(i -> "%s").collect(Collectors.joining(", ", "(", ")"));
    }

    private static final Set<Func> AGGREGATE_VALUES = EnumSet.of(
        COUNT, SUM, AVG, MIN, MAX,
        FIRST, FIRST_NUM, FIRST_STR,
        LAST, LAST_NUM, LAST_STR
    );
}
