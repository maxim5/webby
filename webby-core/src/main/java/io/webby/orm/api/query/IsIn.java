package io.webby.orm.api.query;

import io.webby.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.orm.api.query.InvalidQueryException.assure;
import static io.webby.orm.api.query.Units.COMMA_JOINER;
import static io.webby.orm.api.query.Units.flattenArgsOf;

public class IsIn extends Unit implements BoolTerm {
    public IsIn(@NotNull Term lhs, @NotNull List<? extends Term> rhs) {
        super("%s IN (%s)".formatted(lhs.repr(), rhs.stream().map(Representable::repr).collect(COMMA_JOINER)),
              EasyIterables.concat(lhs.args(), flattenArgsOf(rhs)));
        for (Term term : rhs) {
            assure(TermType.match(lhs.type(), term.type()), "Incompatible types for IN clause: lhs=%s rhs=%s", lhs, term);
        }
    }
}
