package io.webby.util.sql.schema;

import io.webby.util.sql.codegen.ModelAdaptersLocator;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public record PojoSchema(@NotNull Class<?> type, @NotNull List<PojoField> fields) implements WithColumns {
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
            if (field.pojo() != null) {
                field.pojo().iterate(context, consumer);
            }
            context.pop();
        }
    }

    public @NotNull LinkedHashMap<PojoField, Column> columnsPerFields() {
        LinkedHashMap<PojoField, Column> result = new LinkedHashMap<>();
        iterateAllFields(fieldsPath -> {
            PojoField peek = fieldsPath.peek();
            if (peek.isNativelySupported()) {
                String pathName = fieldsPath.stream().map(PojoField::name).map(Naming::camelToSnake).collect(Collectors.joining("_"));
                JdbcType type = requireNonNull(peek.jdbcType());
                Column column = new Column(pathName, new ColumnType(type));
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
