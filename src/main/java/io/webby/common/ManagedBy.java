package io.webby.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ManagedBy(@NotNull Owner owner, @Nullable Lifetime lifetime) {
    public static final ManagedBy MANUALLY_BY_CALLER = new ManagedBy(Owner.CALLER, null);
    public static final ManagedBy BY_PROVIDER = new ManagedBy(Owner.PROVIDER, null);

    public static @NotNull ManagedBy byLifetime(@NotNull Lifetime lifetime) {
        return new ManagedBy(Owner.GIVEN, lifetime);
    }

    public enum Owner {
        CALLER,
        PROVIDER,
        GIVEN
    }
}
