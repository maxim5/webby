package io.webby.orm.arch.factory;

import io.webby.orm.arch.JdbcType;
import io.webby.orm.arch.model.TableArch;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;

import static io.webby.orm.arch.InvalidSqlModelException.failIf;
import static java.util.Objects.requireNonNull;

class FieldResolver {
    private final RunContext runContext;
    private final ForeignTableArchResolver foreignTableArchResolver;

    public FieldResolver(@NotNull RunContext runContext) {
        this.runContext = runContext;
        this.foreignTableArchResolver = new ForeignTableArchResolver(runContext);
    }

    public @NotNull FieldResolver.ResolveResult resolve(@NotNull Field field) {
        JdbcType jdbcType = JdbcType.findByMatchingNativeType(field.getType());
        if (jdbcType != null) {
            return ResolveResult.ofNative(jdbcType);
        }

        Pair<TableArch, JdbcType> foreignTableInfo = foreignTableArchResolver.findForeignTableInfo(field);
        if (foreignTableInfo != null) {
            return ResolveResult.ofForeignKey(foreignTableInfo);
        }

        Class<?> mapperClass = JavaClassAnalyzer.getViaClass(field);
        if (mapperClass != null) {
            return ResolveResult.ofMapper(mapperClass);
        }

        Class<?> adapterClass = runContext.adaptersScanner().locateAdapterClass(field.getType());
        if (adapterClass != null) {
            return ResolveResult.ofAdapter(adapterClass);
        }

        validateFieldForPojo(field);
        return ResolveResult.ofPojo();
    }

    record ResolveResult(@NotNull ResultType type,
                         @Nullable JdbcType jdbcType,
                         @Nullable Pair<TableArch, JdbcType> foreignTable,
                         @Nullable Class<?> mapperClass,
                         @Nullable Class<?> adapterClass) {
        public static @NotNull FieldResolver.ResolveResult ofNative(@NotNull JdbcType jdbcType) {
            return new ResolveResult(ResultType.NATIVE, jdbcType, null, null, null);
        }

        public static @NotNull FieldResolver.ResolveResult ofForeignKey(@NotNull Pair<TableArch, JdbcType> foreignTable) {
            return new ResolveResult(ResultType.FOREIGN_KEY, null, foreignTable, null, null);
        }

        public static @NotNull FieldResolver.ResolveResult ofAdapter(@NotNull Class<?> adapterClass) {
            return new ResolveResult(ResultType.HAS_ADAPTER, null, null, null, adapterClass);
        }

        public static @NotNull FieldResolver.ResolveResult ofMapper(@NotNull Class<?> mapperClass) {
            return new ResolveResult(ResultType.HAS_MAPPER, null, null, mapperClass, null);
        }

        public static @NotNull FieldResolver.ResolveResult ofPojo() {
            return new ResolveResult(ResultType.POJO, null, null, null, null);
        }

        public @NotNull JdbcType jdbcType() {
            return requireNonNull(jdbcType);
        }

        public @NotNull Pair<TableArch, JdbcType> foreignTable() {
            return requireNonNull(foreignTable);
        }

        public @NotNull Class<?> mapperClass() {
            return requireNonNull(mapperClass);
        }

        public @NotNull Class<?> adapterClass() {
            return requireNonNull(adapterClass);
        }
    }

    enum ResultType {
        NATIVE, FOREIGN_KEY, HAS_MAPPER, HAS_ADAPTER, POJO,
    }

    private static void validateFieldForPojo(@NotNull Field field) {
        Class<?> modelClass = field.getDeclaringClass();
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        String typeName = fieldType.getSimpleName();

        failIf(fieldType.isInterface(), "Model class `%s` contains an interface field `%s` without a matching adapter: %s",
               modelClass, typeName, fieldName);
        failIf(Collection.class.isAssignableFrom(fieldType), "Model class `%s` contains a collection field: %s",
               modelClass, fieldName);
        failIf(fieldType.isArray(), "Model class `%s` contains an array field `%s` without a matching adapter: %s",
               modelClass, typeName, fieldName);
        failIf(fieldType == Object.class, "Model class `%s` contains a `%s` field: %s",
               modelClass, typeName, fieldName);
    }
}
