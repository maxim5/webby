package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public record PojoField(@NotNull Field field,
                        @NotNull Method getter,
                        @Nullable JdbcType jdbcType,
                        @Nullable PojoSchema pojo) {
    public static @NotNull PojoField ofNative(@NotNull Field field, @NotNull Method getter, @NotNull JdbcType jdbcType) {
        return new PojoField(field, getter, jdbcType, null);
    }

    public static @NotNull PojoField ofSubPojo(@NotNull Field field, @NotNull Method getter, @NotNull PojoSchema pojo) {
        return new PojoField(field, getter, null, pojo);
    }

    public @NotNull String name() {
        return field.getName();
    }

    public boolean isNativelySupported() {
        return jdbcType != null;
    }
}
