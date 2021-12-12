package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.webby.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrderBy extends Unit implements Clause, Representable {
    private final ImmutableList<OrderTerm> terms;

    public OrderBy(@NotNull List<OrderTerm> terms) {
        super("ORDER BY %s".formatted(joinWithCommas(terms)), flattenArgsOf(terms.stream().map(OrderTerm::term).toList()));
        this.terms = ImmutableList.copyOf(terms);
    }

    public static @NotNull OrderBy of(@NotNull Term term, @NotNull Order order) {
        return of(new OrderTerm(term, order));
    }

    public static @NotNull OrderBy of(@NotNull Order order, @NotNull List<Term> terms) {
        return of(terms.stream().map(term -> new OrderTerm(term, order)).collect(ImmutableList.toImmutableList()));
    }

    public static @NotNull OrderBy of(@NotNull Order order, @NotNull Term @NotNull ... terms) {
        return of(Arrays.stream(terms).map(term -> new OrderTerm(term, order)).collect(ImmutableList.toImmutableList()));
    }

    public static @NotNull OrderBy of(@NotNull List<OrderTerm> terms) {
        return new OrderBy(terms);
    }

    public static @NotNull OrderBy of(@NotNull OrderTerm @NotNull ... terms) {
        return of(ImmutableList.copyOf(terms));
    }

    public @NotNull OrderBy withOneMoreTerm(@NotNull Term term, @NotNull Order order) {
        return withMoreTerms(List.of(new OrderTerm(term, order)));
    }

    public @NotNull OrderBy withMoreTerms(@NotNull List<OrderTerm> terms) {
        return of(EasyIterables.concat(this.terms, terms));
    }

    @NotNull ImmutableList<OrderTerm> terms() {
        return terms;
    }

    private static @NotNull String joinWithCommas(@NotNull List<? extends Representable> terms) {
        return terms.stream().map(Representable::repr).collect(Collectors.joining(", "));
    }
}
