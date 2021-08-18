package io.webby.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;

public class EasyClasspath {
    public static @Nullable Class<?> classForNameOrNull(@NotNull String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }

    public static boolean isInClassPath(@NotNull String name) {
        return classForNameOrNull(name) != null;
    }

    public static @Nullable Method findMethod(@NotNull String className, @NotNull String methodName) {
        return findMethod(classForNameOrNull(className), methodName);
    }

    public static @Nullable Method findMethod(@Nullable Class<?> klass, @NotNull String methodName) {
        return klass == null ? null :
                Arrays.stream(klass.getDeclaredMethods())
                        .filter(method -> method.getName().equals(methodName))
                        .findAny()
                        .orElse(null);
    }
}
