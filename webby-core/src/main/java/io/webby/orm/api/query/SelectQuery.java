package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public interface SelectQuery extends Representable, HasArgs {
    int columnsNumber();

    default @NotNull SelectQuery intern() {
        return HardcodedSelectQuery.of(repr(), args());
    }
}
