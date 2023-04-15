package io.webby.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.webby.orm.arch.factory.FieldResolver.ResolveResult;
import io.webby.orm.arch.model.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import static io.webby.orm.arch.InvalidSqlModelException.failIf;

class RecursivePojoArchFactory {
    private final RunContext runContext;
    private final FieldResolver fieldResolver;

    public RecursivePojoArchFactory(@NotNull RunContext runContext) {
        this.runContext = runContext;
        this.fieldResolver = new FieldResolver(runContext);
    }

    public @NotNull PojoArch buildPojoArchFor(@NotNull Field field) {
        return runContext.pojos().getOrCompute(field.getType(), () -> buildPojoArchForImpl(field));
    }

    private @NotNull PojoArch buildPojoArchForImpl(@NotNull Field field) {
        validateFieldForPojo(field);
        Class<?> type = field.getType();

        if (type.isEnum()) {
            return new PojoArch(type, ImmutableList.of(PojoFieldNative.ofEnum(type)));
        }

        ImmutableList<PojoField> pojoFields = JavaClassAnalyzer.getAllFieldsOrdered(type).stream().map(subField -> {
            Method getter = JavaClassAnalyzer.findGetterMethodOrDie(subField);
            ModelField modelField = ModelField.of(subField, getter);

            ResolveResult resolved = fieldResolver.resolve(subField);
            return switch (resolved.type()) {
                case NATIVE -> PojoFieldNative.ofNative(modelField, resolved.jdbcType());
                case FOREIGN_KEY -> null;
                case ADAPTER -> PojoFieldAdapter.ofAdapter(modelField, resolved.adapterClass());
                case POJO -> {
                    PojoArch nestedPojo = buildPojoArchFor(subField);
                    yield PojoFieldNested.ofNestedPojo(modelField, nestedPojo);
                }
            };
        }).collect(ImmutableList.toImmutableList());
        return new PojoArch(type, pojoFields);
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
