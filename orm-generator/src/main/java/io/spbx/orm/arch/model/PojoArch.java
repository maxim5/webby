package io.spbx.orm.arch.model;

import com.google.common.collect.ImmutableList;
import io.spbx.orm.arch.util.Naming;
import io.spbx.util.base.EasyExceptions.IllegalStateExceptions;
import io.spbx.util.lazy.AtomicCacheCompute;
import io.spbx.util.lazy.CacheCompute;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static io.spbx.orm.arch.model.JavaNameValidator.validateJavaIdentifier;

public final class PojoArch implements HasColumns {
    private final @NotNull Class<?> pojoType;
    private final @NotNull ImmutableList<PojoField> fields;
    private final CacheCompute<List<Column>> columnsCache = AtomicCacheCompute.createEmpty();

    public PojoArch(@NotNull Class<?> pojoType, @NotNull ImmutableList<PojoField> fields) {
        this.pojoType = pojoType;
        this.fields = fields;
    }

    public @NotNull Class<?> pojoType() {
        return pojoType;
    }

    public @NotNull ImmutableList<PojoField> fields() {
        return fields;
    }

    public @NotNull String adapterName() {
        return validateJavaIdentifier(Naming.defaultAdapterName(pojoType));
    }

    public void iterateAllFields(@NotNull Consumer<PojoField> consumer) {
        for (PojoField field : fields) {
            consumer.accept(field);
            if (field instanceof PojoFieldNested fieldNested) {
                fieldNested.pojo().iterateAllFields(consumer);
            }
        }
    }

    @Override
    public @NotNull List<Column> columns() {
        return columnsCache.getOrCompute(() -> {
            ArrayList<Column> result = new ArrayList<>();
            iterateAllFields(field -> {
                if (field instanceof PojoFieldNative fieldNative) {
                    result.add(fieldNative.column());
                } else if (field instanceof PojoFieldMapper fieldMapper) {
                    result.add(fieldMapper.column());
                } else if (field instanceof PojoFieldAdapter fieldAdapter) {
                    result.addAll(fieldAdapter.columns());
                } else if (field instanceof PojoFieldNested ignore) {
                    // no columns for this field (nested to be processed later)
                } else {
                    throw IllegalStateExceptions.format("Internal error. Unrecognized field: %s", field);
                }
            });
            return result;
        });
    }

    public @NotNull PojoArch reattachedTo(@NotNull PojoParent parent) {
        ImmutableList<PojoField> reattachedFields = fields.stream()
            .map(field -> field.reattachedTo(parent))
            .collect(ImmutableList.toImmutableList());
        return new PojoArch(pojoType, reattachedFields);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoArch that &&
               Objects.equals(this.pojoType, that.pojoType) && Objects.equals(this.fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pojoType, fields);
    }

    @Override
    public String toString() {
        return "PojoArch[type=%s, fields=%s]".formatted(pojoType, fields);
    }
}
