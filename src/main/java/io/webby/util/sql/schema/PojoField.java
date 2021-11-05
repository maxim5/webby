package io.webby.util.sql.schema;

import io.webby.util.EasyClasspath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public record PojoField(@NotNull String name,
                        @NotNull Method getter,
                        @Nullable JdbcType jdbcType,
                        @Nullable PojoSchema pojo) {
    public static @NotNull PojoField ofNative(@NotNull Field field, @NotNull Method getter, @NotNull JdbcType jdbcType) {
        return new PojoField(field.getName(), getter, jdbcType, null);
    }

    public static @NotNull PojoField ofSubPojo(@NotNull Field field, @NotNull Method getter, @NotNull PojoSchema pojo) {
        return new PojoField(field.getName(), getter, null, pojo);
    }

    public static @NotNull PojoField ofEnum(@NotNull Class<?> type) {
        assert type.isEnum() : "Type is not an enum: %s".formatted(type);
        Method getter = requireNonNull(EasyClasspath.findMethod(type, EasyClasspath.Scope.ALL, "ordinal"));
        return new PojoField("ord", getter, JdbcType.Int, null);
    }

    public boolean isNativelySupported() {
        return jdbcType != null;
    }
}
