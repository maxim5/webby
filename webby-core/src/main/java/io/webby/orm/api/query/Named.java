package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public interface Named extends Term {
    @NotNull String name();

    @Override
    default @NotNull Named namedAs(@NotNull String name) {
        return name().equals(name) ? this : new NamedAs(this, name);
    }
}
