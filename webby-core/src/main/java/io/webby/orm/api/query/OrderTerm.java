package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a term with an order for the higher <code>ORDER BY</code>.
 */
public record OrderTerm(@NotNull Term term, @NotNull Order order) implements Representable {
    public static @NotNull OrderTerm ofAsc(@NotNull Term term) {
        return new OrderTerm(term, Order.ASC);
    }

    public static @NotNull OrderTerm ofDesc(@NotNull Term term) {
        return new OrderTerm(term, Order.DESC);
    }

    @Override
    public @NotNull String repr() {
        return "%s %s".formatted(term.repr(), order);
    }
}
