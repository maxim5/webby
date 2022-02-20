package io.webby.orm.arch;

import com.google.mu.util.stream.BiStream;
import io.webby.orm.adapter.JdbcAdapt;
import io.webby.util.base.EasyPrimitives.MutableInt;
import io.webby.util.collect.OneOf;
import io.webby.util.lazy.AtomicLazy;
import io.webby.util.lazy.DelayedAccessLazy;
import io.webby.util.reflect.EasyAnnotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.webby.util.reflect.EasyMembers.*;
import static io.webby.orm.arch.InvalidSqlModelException.failIf;

public class AdapterApi {
    public static final String CREATE_INSTANCE = "createInstance";
    public static final String FILL_VALUES = "fillArrayValues";
    public static final String NEW_ARRAY = "toNewValuesArray";

    private final @NotNull OneOf<Class<?>, PojoArch> oneOf;

    private final DelayedAccessLazy<String> staticClassRef = AtomicLazy.emptyLazy();

    private AdapterApi(@NotNull OneOf<Class<?>, PojoArch> oneOf) {
        this.oneOf = oneOf;
    }

    public static @Nullable AdapterApi ofClass(@Nullable Class<?> adapterClass) {
        return adapterClass != null ? new AdapterApi(OneOf.ofFirst(adapterClass)) : null;
    }

    public static @NotNull AdapterApi ofSignature(@NotNull PojoArch pojoArch) {
        return new AdapterApi(OneOf.ofSecond(pojoArch));
    }

    public @NotNull String staticRef() {
        return staticClassRef.lazyGet(() -> oneOf.mapToObj(AdapterApi::classToStaticRef, AdapterApi::signatureStaticRef));
    }

    private static @NotNull String classToStaticRef(@NotNull Class<?> klass) {
        String canonicalName = Naming.shortCanonicalJavaName(klass);

        if (hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals(CREATE_INSTANCE)) &&
            hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals(FILL_VALUES)) &&
            hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals(NEW_ARRAY))) {
            return canonicalName;
        }

        Field staticField = findField(klass, field -> isPublicStatic(field) && field.getType().isAssignableFrom(klass));
        if (staticField != null) {
            return "%s.%s".formatted(canonicalName, staticField.getName());
        }

        return "new %s()".formatted(canonicalName);
    }

    private static @NotNull String signatureStaticRef(@NotNull PojoArch pojoArch) {
        return "%s.ADAPTER".formatted(pojoArch.adapterName());
    }

    public @NotNull List<Column> adapterColumns(@NotNull String fieldSqlName) {
        return oneOf.mapToObj(klass -> classToAdapterColumns(klass, fieldSqlName), pojo -> pojoToColumns(pojo, fieldSqlName));
    }

    public int adapterColumnsNumber() {
        return oneOf.mapToInt(klass -> getCreationParameters(klass).length, HasColumns::columnsNumber);
    }

    @VisibleForTesting
    static @NotNull List<Column> classToAdapterColumns(@NotNull Class<?> klass, @NotNull String fieldSqlName) {
        Parameter[] parameters = getCreationParameters(klass);
        if (parameters.length == 1) {
            JdbcType paramType = JdbcType.findByMatchingNativeType(parameters[0].getType());
            failIf(paramType == null, "JDBC adapter `%s` has incompatible parameters: %s", CREATE_INSTANCE, klass);
            Column column = new Column(fieldSqlName, new ColumnType(paramType));
            return List.of(column);
        } else {
            Optional<String[]> names = EasyAnnotations.getOptionalAnnotation(klass, JdbcAdapt.class).map(JdbcAdapt::names);
            MutableInt count = new MutableInt();
            return BiStream.from(Arrays.stream(parameters),
                                 param -> param,
                                 param -> JdbcType.findByMatchingNativeType(param.getType()))
                .mapKeys(param -> Naming.concatSqlNames(
                    fieldSqlName,
                    names.map(array -> array[count.value++]).orElseGet(() -> Naming.fieldSqlName(param)))
                )
                .mapValues(ColumnType::new)
                .mapToObj(Column::new)
                .toList();
        }
    }

    private static @NotNull List<Column> pojoToColumns(@NotNull PojoArch pojo, @NotNull String fieldName) {
        return pojo.reattachedTo(PojoParent.ofTerminal(fieldName, Naming.fieldSqlName(fieldName))).columns();
    }

    private static @NotNull Parameter[] getCreationParameters(@NotNull Class<?> adapterClass) {
        Method method = findMethod(adapterClass, Scope.DECLARED, CREATE_INSTANCE);
        failIf(method == null, "JDBC adapter does not implement `%s` method: %s", CREATE_INSTANCE, adapterClass);
        return method.getParameters();
    }
}
