package io.webby.util.sql.schema;

import com.google.common.collect.ImmutableList;
import io.webby.util.sql.codegen.ModelAdaptersLocator;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record PojoSchema(@NotNull Class<?> type, @NotNull ImmutableList<PojoField> fields) implements WithColumns {
    public @NotNull String adapterName() {
        return ModelAdaptersLocator.defaultAdapterName(type);
    }

    public boolean isEnum() {
        return type.isEnum();
    }

    public void iterateAllFields(@NotNull Consumer<Stack<PojoField>> consumer) {
        iterate(new Stack<>(), consumer);
    }

    private void iterate(@NotNull Stack<PojoField> context, Consumer<Stack<PojoField>> consumer) {
        for (PojoField field : fields) {
            context.push(field);
            consumer.accept(context);
            if (field.hasPojo()) {
                field.pojoOrDie().iterate(context, consumer);
            }
            context.pop();
        }
    }

    public @NotNull Map<PojoField, Column> columnsPerFields() {
        Map<PojoField, Column> result = new LinkedHashMap<>();
        iterateAllFields(fieldsPath -> {
            PojoField peek = fieldsPath.peek();
            if (peek.isNativelySupported()) {
                String pathName = fieldsPath.stream().map(PojoField::javaName).map(Naming::camelToSnake).collect(Collectors.joining("_"));
                JdbcType type = peek.jdbcTypeOrDie();
                Column column = new Column(pathName, new ColumnType(type));
                assert !result.containsKey(peek) :
                        "Internal error. Several columns for one field: `%s` of `%s`: %s, %s"
                        .formatted(peek, type(), column, result.get(peek));
                result.put(peek, column);
            }
        });
        return result;
    }

    @Override
    public @NotNull List<Column> columns() {
        return columnsPerFields().values().stream().toList();
    }
}
