package io.webby.util.sql.schema;

import io.webby.util.EasyClasspath;
import io.webby.util.OneOf;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public record PojoField(@NotNull ModelField field, @NotNull OneOf<JdbcType, PojoSchema> ref) {
    public static @NotNull PojoField ofNative(@NotNull Field field, @NotNull Method getter, @NotNull JdbcType jdbcType) {
        return new PojoField(ModelField.of(field, getter), OneOf.ofFirst(jdbcType));
    }

    public static @NotNull PojoField ofSubPojo(@NotNull Field field, @NotNull Method getter, @NotNull PojoSchema pojo) {
        return new PojoField(ModelField.of(field, getter), OneOf.ofSecond(pojo));
    }

    public static @NotNull PojoField ofEnum(@NotNull Class<?> type) {
        assert type.isEnum() : "Type is not an enum: %s".formatted(type);
        Method getter = requireNonNull(EasyClasspath.findMethod(type, EasyClasspath.Scope.ALL, "ordinal"));
        return new PojoField(new ModelField("ord", getter.getName(), type, type), OneOf.ofFirst(JdbcType.Int));
    }

    public @NotNull String javaName() {
        return field.name();
    }

    public @NotNull String javaGetter() {
        return field.getter();
    }

    public boolean isNativelySupported() {
        return ref.hasFirst();
    }

    public @NotNull JdbcType jdbcTypeOrDie() {
        return requireNonNull(ref.first());
    }

    public boolean hasPojo() {
        return ref.hasSecond();
    }

    public @NotNull PojoSchema pojoOrDie() {
        return requireNonNull(ref.second());
    }
}
