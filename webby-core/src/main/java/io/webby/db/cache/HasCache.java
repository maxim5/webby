package io.webby.db.cache;

import org.jetbrains.annotations.NotNull;

public interface HasCache<C> {
    @NotNull C cache();
}
