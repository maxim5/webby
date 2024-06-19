package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an error related to query preparation or execution.
 */
public class InvalidQueryException extends RuntimeException {
    public InvalidQueryException(@NotNull String message) {
        super(message);
    }

    public InvalidQueryException(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(message.formatted(args));
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new InvalidQueryException(message, args);
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new InvalidQueryException(message, args);
        }
    }
}
