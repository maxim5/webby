package io.webby.orm.arch;

import com.google.common.collect.ImmutableList;
import io.webby.util.lazy.AtomicLazy;
import io.webby.util.lazy.DelayedAccessLazy;
import io.webby.orm.codegen.ModelAdaptersScanner;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class PojoArch implements WithColumns {
    private final @NotNull Class<?> pojoType;
    private final @NotNull ImmutableList<PojoField> fields;
    private final DelayedAccessLazy<List<Column>> columnsRef = AtomicLazy.emptyLazy();

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
        return ModelAdaptersScanner.defaultAdapterName(pojoType);  // TODO: refactor?
    }

    public boolean isEnum() {
        return pojoType.isEnum();
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
        return columnsRef.lazyGet(() -> {
            ArrayList<Column> result = new ArrayList<>();
            iterateAllFields(field -> {
                if (field instanceof PojoFieldNative fieldNative) {
                    result.add(fieldNative.column());
                } else if (field instanceof PojoFieldAdapter fieldAdapter) {
                    result.addAll(fieldAdapter.columns());
                } else if (field instanceof PojoFieldNested ignore) {
                    // no columns for this field (nested to be processed later)
                } else {
                    throw new IllegalStateException("Internal error. Unrecognized field: " + field);
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
