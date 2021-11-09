package io.webby.util.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
