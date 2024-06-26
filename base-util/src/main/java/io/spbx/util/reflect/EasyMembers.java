package io.spbx.util.reflect;

import io.spbx.util.classpath.EasyClasspath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.function.Predicate;

import static io.spbx.util.base.EasyCast.castAny;

public class EasyMembers {
    public static boolean isPublicStatic(@NotNull Member member) {
        return hasModifiers(member, Modifier.STATIC | Modifier.PUBLIC);
    }

    public static boolean isPublic(@NotNull Member member) {
        return hasModifiers(member, Modifier.PUBLIC);
    }

    public static boolean isPrivate(@NotNull Member member) {
        return hasModifiers(member, Modifier.PRIVATE);
    }

    public static boolean isStatic(@NotNull Member member) {
        return hasModifiers(member, Modifier.STATIC);
    }

    public static boolean hasModifiers(@NotNull Member member, int modifiers) {
        return (modifiers & member.getModifiers()) == modifiers;
    }

    public static boolean hasType(@NotNull Field field, @NotNull Class<?> type) {
        return type.isAssignableFrom(field.getType());
    }

    public static boolean hasReturnType(@NotNull Method method, @NotNull Class<?> type) {
        return type.isAssignableFrom(method.getReturnType());
    }

    public static @Nullable Method findMethod(@NotNull String className, @NotNull Scope scope, @NotNull String methodName) {
        return findMethod(EasyClasspath.classForNameOrNull(className), scope, methodName);
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

    public static @Nullable Field findField(@Nullable Class<?> klass, @NotNull String fieldName) {
        return findField(klass, field -> field.getName().equals(fieldName));
    }

    public static @Nullable Field findField(@Nullable Class<?> klass, int modifiers) {
        return findField(klass, field -> hasModifiers(field, modifiers));
    }

    public static @Nullable Field findField(@Nullable Class<?> klass, @NotNull Predicate<Field> predicate) {
        return klass != null ? Arrays.stream(klass.getDeclaredFields()).filter(predicate).findAny().orElse(null) : null;
    }

    public static @Nullable Field findPublicStaticInstance(@NotNull Class<?> klass) {
        return findField(klass, field -> isPublicStatic(field) && hasType(field, klass));
    }

    public static <T> @Nullable T newInstanceOrNull(@Nullable Class<T> klass) {
        if (klass == null) {
            return null;
        }
        try {
            Field instance = findPublicStaticInstance(klass);
            if (instance != null) {
                return castAny(instance.get(klass));
            }

            Constructor<T> constructor = klass.getConstructor();
            if (!isPrivate(constructor)) {
                return constructor.newInstance();
            }

            Method staticConstructor =
                findMethod(klass, Scope.DECLARED, method -> isPublicStatic(method) && hasReturnType(method, klass));
            if (staticConstructor != null) {
                return castAny(staticConstructor.invoke(klass));
            }
        } catch (Throwable ignore) {
        }
        return null;
    }

    public static <T> @Nullable T newInstanceOrNull(@NotNull String className) {
        Class<T> klass = castAny(EasyClasspath.classForNameOrNull(className));
        return EasyMembers.newInstanceOrNull(klass);
    }

    public static @Nullable Constructor<?> findConstructor(@Nullable Class<?> klass,
                                                           @NotNull Scope scope,
                                                           @NotNull Predicate<Constructor<?>> predicate) {
        return klass != null ?
            Arrays.stream(scope == Scope.DECLARED ? klass.getDeclaredConstructors() : klass.getConstructors())
                .filter(predicate)
                .findAny()
                .orElse(null) :
            null;
    }

    public enum Scope {
        DECLARED,
        ALL,
    }
}
