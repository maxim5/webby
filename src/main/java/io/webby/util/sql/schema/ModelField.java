package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public record ModelField(@NotNull String name,
                         @NotNull String getter,
                         @NotNull Class<?> type,
                         @NotNull Class<?> container) {
    public static @NotNull ModelField of(@NotNull Field field, @NotNull Method getter) {
        assert field.getType() == getter.getReturnType() :
                "Incompatible field and getter types: `%s` vs `%s`".formatted(field, getter);
        assert field.getDeclaringClass() == getter.getDeclaringClass() :
                "Incompatible field and getter container classes: `%s` vs `%s`".formatted(field, getter);
        return new ModelField(field.getName(), getter.getName(), field.getType(), field.getDeclaringClass());
    }
}
