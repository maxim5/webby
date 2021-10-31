package io.webby.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Predicate;

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

    public static boolean isPublicStatic(@NotNull Member member) {
        return hasModifiers(member, Modifier.STATIC | Modifier.PUBLIC);
    }

    public static boolean hasModifiers(@NotNull Member member, int modifiers) {
        return (modifiers & member.getModifiers()) == modifiers;
    }

    public static @Nullable Method findMethod(@NotNull String className, @NotNull String methodName) {
        return findMethod(classForNameOrNull(className), methodName);
    }

    public static @Nullable Method findMethod(@Nullable Class<?> klass, @NotNull String methodName) {
        return findMethod(klass, method -> method.getName().equals(methodName));
    }

    public static @Nullable Method findMethod(@Nullable Class<?> klass, @NotNull Predicate<Method> predicate) {
        return klass != null ? Arrays.stream(klass.getDeclaredMethods()).filter(predicate).findAny().orElse(null) : null;
    }

    public static boolean hasMethod(@NotNull Class<?> klass, @NotNull Predicate<Method> predicate) {
        return findMethod(klass, predicate) != null;
    }

    public static @Nullable Field findField(@Nullable Class<?> klass, int modifiers) {
        return findField(klass, field -> hasModifiers(field, modifiers));
    }

    public static @Nullable Field findField(@Nullable Class<?> klass, @NotNull Predicate<Field> predicate) {
        return klass != null ? Arrays.stream(klass.getDeclaredFields()).filter(predicate).findAny().orElse(null) : null;
    }
}
