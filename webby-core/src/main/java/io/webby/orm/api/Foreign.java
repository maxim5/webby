package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Foreign<I, E> {
    @NotNull I getFk();

    @Nullable E getEntity();

    default boolean hasEntity() {
        return getEntity() != null;
    }

    boolean setEntityIfMissing(@NotNull E entity);

    void setEntityUnconditionally(@NotNull E entity);
}
