package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.spbx.orm.arch.InvalidSqlModelException;
import io.spbx.orm.arch.factory.FieldResolver.ResolveResult;
import io.spbx.orm.arch.model.*;
import io.spbx.orm.arch.util.AnnotationsAnalyzer;
import io.spbx.orm.arch.util.JavaClassAnalyzer;
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
        return buildPojoArchFor(field.getType());
    }

    public @NotNull PojoArch buildPojoArchFor(@NotNull Class<?> klass) {
        return runContext.pojos().getOrCompute(klass, () -> buildPojoArchForImpl(klass));
    }

    private @NotNull PojoArch buildPojoArchForImpl(@NotNull Class<?> klass) {
        ImmutableList<PojoField> pojoFields = JavaClassAnalyzer.getAllFieldsOrdered(klass).stream().map(subField -> {
            ModelField modelField = JavaClassAnalyzer.toModelField(subField);
            ResolveResult resolved = fieldResolver.resolve(subField);
            return switch (resolved.type()) {
                case NATIVE -> PojoFieldNative.ofNative(modelField, resolved.jdbcType());
                case FOREIGN_KEY ->
                    throw new InvalidSqlModelException("Foreign keys in nested classes are not supported: %s", subField);
                case HAS_MAPPER -> {
                    MapperApi mapperApi = MapperApi.ofExistingMapper(resolved.mapperClass(),
                                                                     subField.getGenericType(),
                                                                     AnnotationsAnalyzer.isNullableField(subField));
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
        return new PojoArch(klass, pojoFields);
    }
}
