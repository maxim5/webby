package io.webby.orm.codegen;

import io.webby.orm.arch.model.JavaNameHolder;
import io.webby.orm.arch.util.Naming;
import org.jetbrains.annotations.NotNull;

import static io.webby.orm.arch.model.JavaNameValidator.validateJavaIdentifier;
import static io.webby.orm.arch.model.JavaNameValidator.validateJavaPackage;

public record FQN(@NotNull String packageName, @NotNull String className) {
    public FQN {
        validateJavaIdentifier(className);
        validateJavaPackage(packageName);
    }

    public static @NotNull FQN of(@NotNull Class<?> klass) {
        return new FQN(klass.getPackageName(), Naming.shortCanonicalJavaName(klass));
    }

    public static @NotNull FQN of(@NotNull JavaNameHolder nameHolder) {
        return new FQN(nameHolder.packageName(), nameHolder.javaName());
    }

    public @NotNull String importName() {
        return "%s.%s".formatted(packageName, className);
    }
}
