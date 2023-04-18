package io.webby.orm.arch.model;

import io.webby.orm.arch.Column;
import io.webby.orm.arch.ColumnType;
import io.webby.orm.arch.InvalidSqlModelException;
import io.webby.orm.arch.JdbcType;
import io.webby.orm.arch.util.Naming;
import io.webby.util.func.Reversible;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static io.webby.orm.arch.InvalidSqlModelException.failIf;
import static io.webby.util.reflect.EasyGenerics.getGenericTypeArgumentsOfInterface;
import static io.webby.util.reflect.EasyGenerics.isWildcardType;
import static io.webby.util.reflect.EasyMembers.*;
import static java.util.Objects.requireNonNull;

public class MapperApi implements ApiFormatter<MapperApi.MapperCallFormatter> {
    private final JdbcType jdbcType;
    private final MapperCallFormatter formatter;

    private MapperApi(@NotNull JdbcType jdbcType, @NotNull MapperCallFormatter formatter) {
        this.jdbcType = jdbcType;
        this.formatter = formatter;
    }

    public static @NotNull MapperApi ofExistingMapper(@NotNull Class<?> mapperClass,
                                                      @NotNull Type fieldType,
                                                      boolean nullable) {
        String staticRef = classToStaticRef(mapperClass);
        MapperClassInference inference = infer(mapperClass, fieldType);
        String forwardCall = (inference.isJdbcFirstArgument() ? "backward" : "forward") + (nullable ? "Nullable" : "");
        String backwardCall = (inference.isJdbcFirstArgument() ? "forward" : "backward") + (nullable ? "Nullable" : "");
        MapperCallFormatter formatter = new MapperCallFormatter() {
            @Override
            public @NotNull String jdbcToField(@NotNull String jdbcParam) {
                return "%s.%s(%s)".formatted(staticRef, forwardCall, jdbcParam);
            }
            @Override
            public @NotNull String fieldToJdbc(@NotNull String fieldParam) {
                return "%s.%s(%s)".formatted(staticRef, backwardCall, fieldParam);
            }
        };
        return new MapperApi(inference.jdbcType(), formatter);
    }

    public static @NotNull MapperApi ofInlineMapper(@NotNull JdbcType jdbcType, @NotNull MapperCallFormatter formatter) {
        return new MapperApi(jdbcType, formatter);
    }

    private record MapperClassInference(@NotNull JdbcType jdbcType, boolean isJdbcFirstArgument) {}

    private static @NotNull MapperClassInference infer(@NotNull Class<?> mapperClass, @NotNull Type fieldType) {
        Type[] types = getGenericTypeArgumentsOfInterface(mapperClass, Reversible.class);
        failIf(types == null, "Mapper class must implement Reversible<U, V> interface: %s", mapperClass);
        failIf(types.length != 2, "Mapper class must have exactly two type arguments: %s", mapperClass);
        failIf(isWildcardType(types[0]) || isWildcardType(types[1]),
               "Mapper class must actualize Reversible<U, V> arguments: %s", mapperClass);

        Type from = types[0];
        Type to = types[1];
        if (fieldType.equals(from)) {
            JdbcType jdbcType = to instanceof Class<?> klass ? JdbcType.findByMatchingNativeType(klass) : null;
            if (jdbcType != null) {
                return new MapperClassInference(jdbcType, true);
            }
        }
        if (fieldType.equals(to)) {
            JdbcType jdbcType = from instanceof Class<?> klass ? JdbcType.findByMatchingNativeType(klass) : null;
            if (jdbcType != null) {
                return new MapperClassInference(jdbcType, false);
            }
        }

        throw new InvalidSqlModelException("Failed to identify mapper class arguments: mapper=%s, field=%s",
                                           mapperClass, fieldType);
    }

    public @NotNull JdbcType jdbcType() {
        return jdbcType;
    }

    public @NotNull Column mapperColumn(@NotNull String fieldSqlName) {
        return new Column(fieldSqlName, new ColumnType(jdbcType));
    }

    @Override
    public @NotNull MapperCallFormatter formatter(@NotNull FormatMode mode) {
        return new MapperCallFormatter() {
            @Override
            public @NotNull String jdbcToField(@NotNull String jdbcParam) {
                return formatter.jdbcToField(jdbcParam) + mode.eol();
            }

            @Override
            public @NotNull String fieldToJdbc(@NotNull String fieldParam) {
                return formatter.fieldToJdbc(fieldParam) + mode.eol();
            }
        };
    }

    private static @NotNull String classToStaticRef(@NotNull Class<?> klass) {
        String canonicalName = Naming.shortCanonicalJavaName(klass);

        if (hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals("forward")) &&
            hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals("backward"))) {
            return canonicalName;
        }

        Field fieldInstance = findField(klass, field -> isPublicStatic(field) && field.getType().isAssignableFrom(klass));
        if (fieldInstance != null) {
            return "%s.%s".formatted(canonicalName, fieldInstance.getName());
        }

        if (klass.isAnonymousClass()) {
            Class<?> enclosingClass = requireNonNull(klass.getEnclosingClass());
            fieldInstance = findField(enclosingClass, field -> isPublicStatic(field) && field.getType().isAssignableFrom(klass));
            if (fieldInstance != null) {
                return "%s.%s".formatted(Naming.shortCanonicalJavaName(enclosingClass), fieldInstance.getName());
            }
        }

        return "new %s()".formatted(canonicalName);
    }

    public interface MapperCallFormatter {
        @NotNull String jdbcToField(@NotNull String jdbcParam);
        @NotNull String fieldToJdbc(@NotNull String fieldParam);
    }
}
