package io.webby.util.sql.schema;

import com.google.common.collect.ImmutableList;
import io.webby.util.sql.codegen.ModelAdaptersLocator;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public record PojoSchema(@NotNull Class<?> pojoType, @NotNull ImmutableList<PojoField> fields) implements WithColumns {
    public @NotNull PojoSchema reattachedTo(@NotNull PojoParent parent) {
        ImmutableList<PojoField> reattachedFields = fields().stream()
                .map(field -> field.reattachedTo(parent))
                .collect(ImmutableList.toImmutableList());
        return new PojoSchema(pojoType, reattachedFields);
    }

    public @NotNull String adapterName() {
        return ModelAdaptersLocator.defaultAdapterName(pojoType);
    }

    public boolean isEnum() {
        return pojoType.isEnum();
    }

    public void iterateAllFields(@NotNull Consumer<PojoField> consumer) {
        for (PojoField field : fields()) {
            consumer.accept(field);
            if (field.hasPojo()) {
                field.pojoOrDie().iterateAllFields(consumer);
            }
        }
    }

    public @NotNull Map<PojoField, Column> columnsPerFields() {
        Map<PojoField, Column> result = new LinkedHashMap<>();
        iterateAllFields(field -> {
            if (field.isNativelySupported()) {
                String sqlName = field.fullSqlName();
                JdbcType type = field.jdbcTypeOrDie();
                Column column = new Column(sqlName, new ColumnType(type));
                assert !result.containsKey(field) :
                        "Internal error. Several columns for one field: `%s` of `%s`: %s, %s"
                        .formatted(field, pojoType(), column, result.get(field));
                result.put(field, column);
            }
        });
        return result;
    }

    @Override
    public @NotNull List<Column> columns() {
        return columnsPerFields().values().stream().toList();
    }
}
