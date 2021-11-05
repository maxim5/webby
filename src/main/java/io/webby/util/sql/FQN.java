package io.webby.util.sql;

import io.webby.util.sql.schema.Naming;
import org.jetbrains.annotations.NotNull;

record FQN(@NotNull String packageName, @NotNull String className) {
    public static @NotNull FQN of(@NotNull Class<?> klass) {
        return new FQN(klass.getPackageName(), Naming.shortCanonicalName(klass));
    }

    public @NotNull String importName() {
        return "%s.%s".formatted(packageName, className);
    }
}
