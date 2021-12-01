package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import static io.webby.orm.api.query.InvalidQueryException.assure;

public class Like extends Unit implements BoolTerm {
    public Like(@NotNull Term lhs, @NotNull Term rhs) {
        super("%s LIKE %s".formatted(lhs.repr(), rhs.repr()), flattenArgsOf(lhs, rhs));
        assure(TermType.match(lhs.type(), rhs.type(), TermType.STRING),
               "Incompatible types to like: lhs=%s rhs=%s", lhs, rhs);
    }
}
