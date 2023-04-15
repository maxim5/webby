package io.webby.orm.arch.factory;

import io.webby.orm.arch.JdbcType;
import io.webby.orm.arch.model.TableArch;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

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

        Class<?> adapterClass = runContext.adaptersScanner().locateAdapterClass(field.getType());
        if (adapterClass != null) {
            return ResolveResult.ofAdapter(adapterClass);
        }

        return ResolveResult.ofPojo();
    }

    record ResolveResult(@NotNull ResultType type,
                         @Nullable JdbcType jdbcType,
                         @Nullable Pair<TableArch, JdbcType> foreignTable,
                         @Nullable Class<?> adapterClass) {
        public static @NotNull FieldResolver.ResolveResult ofNative(@NotNull JdbcType jdbcType) {
            return new ResolveResult(ResultType.NATIVE, jdbcType, null, null);
        }

        public static @NotNull FieldResolver.ResolveResult ofForeignKey(@NotNull Pair<TableArch, JdbcType> foreignTable) {
            return new ResolveResult(ResultType.FOREIGN_KEY, null, foreignTable, null);
        }

        public static @NotNull FieldResolver.ResolveResult ofAdapter(@NotNull Class<?> adapterClass) {
            return new ResolveResult(ResultType.ADAPTER, null, null, adapterClass);
        }

        public static @NotNull FieldResolver.ResolveResult ofPojo() {
            return new ResolveResult(ResultType.POJO, null, null, null);
        }

        public @NotNull JdbcType jdbcType() {
            return requireNonNull(jdbcType);
        }

        public @NotNull Pair<TableArch, JdbcType> foreignTable() {
            return requireNonNull(foreignTable);
        }

        public @NotNull Class<?> adapterClass() {
            return requireNonNull(adapterClass);
        }
    }

    enum ResultType {
        NATIVE, FOREIGN_KEY, ADAPTER, POJO,
    }
}
