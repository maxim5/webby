package io.webby.url.convert;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConversionError extends RuntimeException {
    private final String var;

    public ConversionError(@NotNull String message) {
        this(null, message);
    }

    public ConversionError(@NotNull String message, @NotNull Throwable cause) {
        this(null, message, cause);
    }

    public ConversionError(@NotNull Throwable cause) {
        this(null, cause.getMessage(), cause);
    }

    public ConversionError(@Nullable String var, @NotNull String message) {
        super(joinMessage(var, message));
        this.var = var;
    }

    public ConversionError(@Nullable String var, @NotNull String message, @NotNull Throwable cause) {
        super(joinMessage(var, message), cause);
        this.var = var;
    }

    public boolean hasVariable() {
        return var != null;
    }

    public static void assure(boolean cond, @Nullable String name, @NotNull String message,
                              @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new ConversionError(name, message.formatted(args));
        }
    }

    public static void failIf(boolean cond, @Nullable String name, @NotNull String message,
                              @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new ConversionError(name, message.formatted(args));
        }
    }

    private static @NotNull String joinMessage(@Nullable String var, @NotNull String message) {
        return var != null ? "[%s]: %s".formatted(var, message) : message;
    }
}
