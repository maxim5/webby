package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.webby.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class OrderBy extends SimpleRepr implements Clause, Repr {
    private final ImmutableList<Term> terms;

    public OrderBy(@NotNull List<Term> terms) {
        super("ORDER BY %s".formatted(terms.stream().map(Repr::repr).collect(Collectors.joining(", "))));
        this.terms = ImmutableList.copyOf(terms);
    }

    public static @NotNull OrderBy of(@NotNull List<Term> terms) {
        return new OrderBy(terms);
    }

    public static @NotNull OrderBy of(@NotNull Term ... terms) {
        return of(ImmutableList.copyOf(terms));
    }

    public @NotNull OrderBy withOneMoreTerm(@NotNull Term term) {
        return withMoreTerms(List.of(term));
    }

    public @NotNull OrderBy withMoreTerms(@NotNull List<Term> terms) {
        return of(EasyIterables.concat(this.terms, terms));
    }

    @NotNull ImmutableList<Term> terms() {
        return terms;
    }
}
