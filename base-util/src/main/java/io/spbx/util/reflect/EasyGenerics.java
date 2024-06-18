package io.spbx.util.reflect;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class EasyGenerics {
    public static boolean isWildcardType(@NotNull Type type) {
        return type instanceof WildcardType;
    }

    public static @NotNull Type[] getGenericTypeArgumentsOfField(@NotNull Field field) {
        return getGenericTypeArguments(field.getGenericType());
    }

    public static @Nullable Type[] getGenericTypeArgumentsOfInterface(@NotNull Class<?> klass,
                                                                      @NotNull Class<?> interfaceClass) {
        return TypeToken.of(klass).getTypes().interfaces().stream()
            .filter(token -> token.getRawType() == interfaceClass)
            .findFirst()
            .map(TypeToken::getType)
            .map(EasyGenerics::getGenericTypeArguments)
            .orElse(null);
    }

    private static @NotNull Type[] getGenericTypeArguments(@NotNull Type type) {
        return type instanceof ParameterizedType parameterized ? parameterized.getActualTypeArguments() : new Type[0];
    }
}
