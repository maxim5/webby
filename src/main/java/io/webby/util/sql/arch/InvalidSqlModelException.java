package io.webby.util.sql.arch;

import io.webby.app.AppConfigException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvalidSqlModelException extends AppConfigException {
    public InvalidSqlModelException() {
    }

    public InvalidSqlModelException(@NotNull String message) {
        super(message);
    }

    public InvalidSqlModelException(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(message.formatted(args));
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new InvalidSqlModelException(message, args);
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new InvalidSqlModelException(message, args);
        }
    }
}
