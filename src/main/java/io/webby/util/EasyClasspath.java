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

    public static @Nullable Method findMethod(@NotNull String className, @NotNull Scope scope, @NotNull String methodName) {
        return findMethod(classForNameOrNull(className), scope, methodName);
    }

    public static @Nullable Method findMethod(@Nullable Class<?> klass, @NotNull Scope scope, @NotNull String methodName) {
        return findMethod(klass, scope, method -> method.getName().equals(methodName));
    }

    public static @Nullable Method findMethod(@Nullable Class<?> klass, @NotNull Scope scope, @NotNull Predicate<Method> predicate) {
        return klass != null ?
                Arrays.stream(scope == Scope.DECLARED ? klass.getDeclaredMethods() : klass.getMethods())
                        .filter(predicate)
                        .findAny()
                        .orElse(null) :
                null;
    }

    public static boolean hasMethod(@NotNull Class<?> klass, @NotNull Scope scope, @NotNull Predicate<Method> predicate) {
        return findMethod(klass, scope, predicate) != null;
    }

    public static @Nullable Field findField(@Nullable Class<?> klass, @NotNull Scope scope, @NotNull String fieldName) {
        return findField(klass, scope, field -> field.getName().equals(fieldName));
    }

    public static @Nullable Field findField(@Nullable Class<?> klass, @NotNull Scope scope, int modifiers) {
        return findField(klass, scope, field -> hasModifiers(field, modifiers));
    }

    public static @Nullable Field findField(@Nullable Class<?> klass, @NotNull Scope scope, @NotNull Predicate<Field> predicate) {
        return klass != null ?
                Arrays.stream(scope == Scope.DECLARED ? klass.getDeclaredFields() : klass.getFields())
                        .filter(predicate)
                        .findAny()
                        .orElse(null) :
                null;
    }

    public enum Scope {
        DECLARED,
        ALL
    }
}
