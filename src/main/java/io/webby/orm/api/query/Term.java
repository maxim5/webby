package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public interface Term extends Representable, ArgsHolder {
    @NotNull TermType type();

    default @NotNull Named namedAs(@NotNull String name) {
        return new NamedAs(this, name);
    }

    default @NotNull BoolWrapper bool() {
        return new BoolWrapper(this);
    }

    default @NotNull OrderTerm ordered(@NotNull Order order) {
        return new OrderTerm(this, order);
    }
}
