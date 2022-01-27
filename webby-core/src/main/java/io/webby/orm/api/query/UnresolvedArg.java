package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record UnresolvedArg(@NotNull String name, @Nullable Object defaultValue) {
    public static @Nullable Object defaultValueForType(@NotNull TermType type) {
        return switch (type) {
            case NUMBER -> 0;
            case BOOL -> false;
            default -> null;
        };
    }
}
