package io.webby.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.webby.orm.arch.factory.FieldResolver.ResolveResult;
import io.webby.orm.arch.model.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

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
        ImmutableList<PojoField> pojoFields = JavaClassAnalyzer.getAllFieldsOrdered(field.getType()).stream().map(subField -> {
            ModelField modelField = JavaClassAnalyzer.toModelField(subField);
            ResolveResult resolved = fieldResolver.resolve(subField);
            return switch (resolved.type()) {
                case NATIVE -> PojoFieldNative.ofNative(modelField, resolved.jdbcType());
                case FOREIGN_KEY -> null;
                case HAS_MAPPER -> {
                    MapperApi mapperApi = MapperApi.ofExistingMapper(resolved.mapperClass(),
                                                                     subField.getGenericType(),
                                                                     JavaClassAnalyzer.isNullableField(subField));
                    yield PojoFieldMapper.ofMapper(modelField, mapperApi);
                }
                case INLINE_MAPPER -> PojoFieldMapper.ofMapper(modelField, resolved.mapperApi());
                case HAS_ADAPTER -> PojoFieldAdapter.ofAdapter(modelField, resolved.adapterClass());
                case POJO -> {
                    PojoArch nestedPojo = buildPojoArchFor(subField);
                    yield PojoFieldNested.ofNestedPojo(modelField, nestedPojo);
                }
            };
        }).collect(ImmutableList.toImmutableList());
        return new PojoArch(field.getType(), pojoFields);
    }
}
