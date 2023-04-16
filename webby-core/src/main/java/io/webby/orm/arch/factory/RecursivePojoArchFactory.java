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
        Class<?> type = field.getType();
        for (FastTrackPojoBuilder builder : FAST_TRACK_POJO_BUILDERS) {
            if (builder.matches(type)) {
                return builder.buildPojo(field);
            }
        }

        ImmutableList<PojoField> pojoFields = JavaClassAnalyzer.getAllFieldsOrdered(type).stream().map(subField -> {
            ModelField modelField = JavaClassAnalyzer.toModelField(subField);
            ResolveResult resolved = fieldResolver.resolve(subField);
            return switch (resolved.type()) {
                case NATIVE -> PojoFieldNative.ofNative(modelField, resolved.jdbcType());
                case FOREIGN_KEY -> null;
                case HAS_ADAPTER -> PojoFieldAdapter.ofAdapter(modelField, resolved.adapterClass());
                case POJO -> {
                    PojoArch nestedPojo = buildPojoArchFor(subField);
                    yield PojoFieldNested.ofNestedPojo(modelField, nestedPojo);
                }
            };
        }).collect(ImmutableList.toImmutableList());
        return new PojoArch(type, pojoFields);
    }

    private static final ImmutableList<FastTrackPojoBuilder> FAST_TRACK_POJO_BUILDERS = ImmutableList.of(
        new EnumPojoBuilder()
    );

    private interface FastTrackPojoBuilder {
        boolean matches(@NotNull Class<?> type);

        @NotNull PojoArch buildPojo(@NotNull Field field);
    }

    private static class EnumPojoBuilder implements FastTrackPojoBuilder {
        @Override
        public boolean matches(@NotNull Class<?> type) {
            return type.isEnum();
        }
        @Override
        public @NotNull PojoArch buildPojo(@NotNull Field field) {
            return new PojoArch(field.getType(), ImmutableList.of(PojoFieldNative.ofEnum(field.getType())));
        }
    }
}
