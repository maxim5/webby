package io.webby.util.sql.codegen;

import io.webby.util.sql.arch.JavaNameHolder;
import io.webby.util.sql.arch.Naming;
import org.jetbrains.annotations.NotNull;

public record FQN(@NotNull String packageName, @NotNull String className) {
    public static @NotNull FQN of(@NotNull Class<?> klass) {
        return new FQN(klass.getPackageName(), Naming.shortCanonicalName(klass));
    }

    public static @NotNull FQN of(@NotNull JavaNameHolder nameHolder) {
        return new FQN(nameHolder.packageName(), nameHolder.javaName());
    }

    public @NotNull String importName() {
        return "%s.%s".formatted(packageName, className);
    }
}
