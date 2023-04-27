package io.webby.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.orm.api.query.Args.flattenArgsOf;
import static io.webby.orm.api.query.InvalidQueryException.assure;
import static io.webby.orm.api.query.Representables.COMMA_JOINER;

@Immutable
public class IsIn extends Unit implements BoolTerm {
    public IsIn(@NotNull Term lhs, @NotNull List<? extends Term> rhs) {
        super("%s IN (%s)".formatted(lhs.repr(), rhs.stream().map(Representable::repr).collect(COMMA_JOINER)),
              Args.concat(lhs.args(), flattenArgsOf(rhs)));
        for (Term term : rhs) {
            assure(TermType.match(lhs.type(), term.type()), "Incompatible types for IN clause: lhs=%s rhs=%s", lhs, term);
        }
    }

    public static @NotNull IsIn isIn(@NotNull Term lhs, @NotNull Term @NotNull ... terms) {
        return new IsIn(lhs, List.of(terms));
    }

    public static @NotNull IsIn isIn(@NotNull Term lhs, @NotNull List<? extends Term> terms) {
        return new IsIn(lhs, terms);
    }
}
