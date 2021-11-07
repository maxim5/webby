package io.webby.util.sql.schema;

import com.google.mu.util.stream.BiStream;
import io.webby.util.lazy.AtomicLazy;
import io.webby.util.lazy.DelayedAccessLazy;
import io.webby.util.EasyClasspath;
import io.webby.util.OneOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import static io.webby.util.EasyClasspath.*;
import static io.webby.util.sql.schema.InvalidSqlModelException.failIf;

public class AdapterInfo {
    public static final String CREATE = "createInstance";
    public static final String FILL_VALUES = "fillArrayValues";
    public static final String NEW_ARRAY = "toNewValuesArray";

    private final @NotNull OneOf<Class<?>, PojoSchema> oneOf;

    private final DelayedAccessLazy<String> staticClassRef = AtomicLazy.emptyLazy();

    private AdapterInfo(@NotNull OneOf<Class<?>, PojoSchema> oneOf) {
        this.oneOf = oneOf;
    }

    public static @Nullable AdapterInfo ofClass(@Nullable Class<?> adapterClass) {
        return adapterClass != null ? new AdapterInfo(OneOf.ofFirst(adapterClass)) : null;
    }

    public static @NotNull AdapterInfo ofSignature(@NotNull PojoSchema pojoSchema) {
        return new AdapterInfo(OneOf.ofSecond(pojoSchema));
    }

    public @NotNull String staticRef() {
        return staticClassRef.lazyGet(() -> oneOf.fromEither(AdapterInfo::classToStaticRef, AdapterInfo::signatureStaticRef));
    }

    private static @NotNull String classToStaticRef(@NotNull Class<?> klass) {
        String canonicalName = Naming.shortCanonicalName(klass);

        if (hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals(CREATE)) &&
            hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals(FILL_VALUES)) &&
            hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals(NEW_ARRAY))) {
            return canonicalName;
        }

        Field staticField = findField(klass, Scope.DECLARED, field -> isPublicStatic(field) && field.getType().isAssignableFrom(klass));
        if (staticField != null) {
            return "%s.%s".formatted(canonicalName, staticField.getName());
        }

        return "new %s()".formatted(canonicalName);
    }

    private static @NotNull String signatureStaticRef(@NotNull PojoSchema pojoSchema) {
        return "%s.ADAPTER".formatted(pojoSchema.adapterName());
    }

    public @NotNull List<Column> adapterColumns(@NotNull String fieldName) {
        return oneOf.fromEither(klass -> classToAdapterColumns(klass, fieldName), pojo -> pojoToColumns(pojo, fieldName));
    }

    private static @NotNull List<Column> classToAdapterColumns(@NotNull Class<?> klass, @NotNull String fieldName) {
        String sqlFieldName = Naming.camelToSnake(fieldName);
        Parameter[] parameters = getCreationParameters(klass);
        if (parameters.length == 1) {
            JdbcType paramType = JdbcType.findByMatchingNativeType(parameters[0].getType());
            failIf(paramType == null, "JDBC adapter `%s` has incompatible parameters: %s", CREATE, klass);
            Column column = new Column(sqlFieldName, new ColumnType(paramType));
            return List.of(column);
        } else {
            return BiStream.from(Arrays.stream(parameters),
                                 Parameter::getName,
                                 param -> JdbcType.findByMatchingNativeType(param.getType()))
                    .mapKeys(name -> "%s_%s".formatted(sqlFieldName, Naming.camelToSnake(name)))
                    .mapValues(ColumnType::new)
                    .mapToObj(Column::new)
                    .toList();
        }
    }

    private static @NotNull List<Column> pojoToColumns(@NotNull PojoSchema pojo, @NotNull String fieldName) {
        return pojo.reattachedTo(PojoParent.ofTerminal(fieldName)).columns();
    }

    private static @NotNull Parameter[] getCreationParameters(@NotNull Class<?> adapterClass) {
        Method method = EasyClasspath.findMethod(adapterClass, Scope.DECLARED, CREATE);
        failIf(method == null, "JDBC adapter does not implement `%s` method: %s", CREATE, adapterClass);
        return method.getParameters();
    }
}
