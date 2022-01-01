package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import static io.webby.orm.api.query.InvalidQueryException.assure;
import static io.webby.orm.api.query.Units.flattenArgsOf;

public class Between extends Unit implements BoolTerm {
    public Between(@NotNull Term term, @NotNull Term left, @NotNull Term right) {
        super("%s BETWEEN %s AND %s".formatted(term.repr(), left.repr(), right.repr()), flattenArgsOf(term, left, right));
        assure(TermType.match(term.type(), left.type(), right.type()),
               "Incompatible types to filter between: term=%s left=%s right=%s", term, left, right);
    }
}