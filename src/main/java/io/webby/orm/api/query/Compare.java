package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import static io.webby.orm.api.query.InvalidQueryException.assure;
import static io.webby.orm.api.query.Units.flattenArgsOf;

public class Compare extends Unit implements BoolTerm {
    public Compare(@NotNull Term lhs, @NotNull Term rhs, @NotNull CompareType compareType) {
        super("%s %s %s".formatted(lhs.repr(), compareType.repr(), rhs.repr()), flattenArgsOf(lhs, rhs));
        assure(TermType.match(lhs.type(), rhs.type()),
               "Incompatible types to compare: lhs=%s rhs=%s compare=%s", lhs, rhs, compareType);
    }
}
