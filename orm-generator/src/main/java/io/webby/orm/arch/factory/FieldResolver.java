package io.webby.orm.arch.factory;

import io.webby.orm.arch.model.JdbcType;
import io.webby.orm.arch.model.MapperApi;
import io.webby.orm.arch.model.TableArch;
import io.webby.orm.arch.util.AnnotationsAnalyzer;
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

        Class<?> mapperClass = AnnotationsAnalyzer.getViaClass(field);
        if (mapperClass != null) {
            return ResolveResult.ofMapper(mapperClass);
        }

        MapperApi inlineMapperApi = InlineMappers.tryInlineMapperIfSupported(field);
        if (inlineMapperApi != null) {
            return ResolveResult.ofInlineMapper(inlineMapperApi);
        }

        Class<?> adapterClass = runContext.adapters().locateAdapterClass(field.getType());
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
                         @Nullable MapperApi mapperApi,
                         @Nullable Class<?> adapterClass) {
        public static @NotNull FieldResolver.ResolveResult ofNative(@NotNull JdbcType jdbcType) {
            return new ResolveResult(ResultType.NATIVE, jdbcType, null, null, null, null);
        }

        public static @NotNull FieldResolver.ResolveResult ofForeignKey(@NotNull Pair<TableArch, JdbcType> foreignTable) {
            return new ResolveResult(ResultType.FOREIGN_KEY, null, foreignTable, null, null, null);
        }

        public static @NotNull FieldResolver.ResolveResult ofAdapter(@NotNull Class<?> adapterClass) {
            return new ResolveResult(ResultType.HAS_ADAPTER, null, null, null, null, adapterClass);
        }

        public static @NotNull FieldResolver.ResolveResult ofMapper(@NotNull Class<?> mapperClass) {
            return new ResolveResult(ResultType.HAS_MAPPER, null, null, mapperClass, null, null);
        }

        public static @NotNull FieldResolver.ResolveResult ofInlineMapper(@NotNull MapperApi mapperApi) {
            return new ResolveResult(ResultType.INLINE_MAPPER, null, null, null, mapperApi, null);
        }

        public static @NotNull FieldResolver.ResolveResult ofPojo() {
            return new ResolveResult(ResultType.POJO, null, null, null, null, null);
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

        public @NotNull MapperApi mapperApi() {
            return requireNonNull(mapperApi);
        }

        public @NotNull Class<?> adapterClass() {
            return requireNonNull(adapterClass);
        }
    }

    enum ResultType {
        NATIVE,
        FOREIGN_KEY,
        HAS_MAPPER,
        INLINE_MAPPER,
        HAS_ADAPTER,
        POJO,
    }

    private static void validateFieldForPojo(@NotNull Field field) {
        Class<?> fieldType = field.getType();
        String typeName = field.getGenericType().getTypeName();

        failIf(Collection.class.isAssignableFrom(fieldType),
               "Model holds a collection `%s` without a mapper or adapter", typeName);
        failIf(fieldType.isInterface(), "Model holds an interface `%s` without a mapper or adapter", typeName);
        failIf(fieldType.isArray(), "Model holds an array `%s` without a mapper or adapter", typeName);
        failIf(fieldType == Object.class, "Model holds a raw `%s` field without a mapper or adapter", typeName);
    }
}
