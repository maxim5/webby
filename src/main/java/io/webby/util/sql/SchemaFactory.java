package io.webby.util.sql;

import com.google.common.base.CaseFormat;
import com.google.common.primitives.Primitives;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class SchemaFactory {
    private final Collection<DataClassInput> inputs;
    private final Map<DataClassInput, TableSchema> schemas = new HashMap<>();

    public SchemaFactory(@NotNull Collection<DataClassInput> inputs) {
        this.inputs = inputs;
    }

    public @NotNull Collection<TableSchema> buildSchemas() {
        for (DataClassInput input : inputs) {
            schemas.put(input, buildShallowTable(input));
        }
        for (Map.Entry<DataClassInput, TableSchema> entry : schemas.entrySet()) {
            completeTable(entry.getKey(), entry.getValue());
        }
        return schemas.values();
    }

    @VisibleForTesting
    @NotNull TableSchema buildShallowTable(@NotNull DataClassInput input) {
        String sqlName = camelToSnake(input.dataName);
        String javaName = "%sTable".formatted(input.dataName);
        return new TableSchema(sqlName, javaName, input.dataName, input.dataClass, new ArrayList<>());
    }

    @VisibleForTesting
    void completeTable(@NotNull DataClassInput input, @NotNull TableSchema schema) {
        List<TableField> fields = Arrays.stream(input.dataClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> buildTableField(field, input.dataName))
                .toList();
        schema.fields().addAll(fields);
    }

    @VisibleForTesting
    @NotNull TableField buildTableField(@NotNull Field field, @NotNull String dataName) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();

        Method getter = findGetterMethod(field);
        assert getter != null : "No getter found for field: %s".formatted(field);
        boolean isPrimaryKey = fieldName.equals("id") || fieldName.equals(camelUpperToLower(dataName) + "Id");

        // enum, foreign data class, pojo class, known type (DateTime, Instant, ...)
        JdbcType jdbcType = JdbcType.TYPES_BY_CLASS.get(Primitives.unwrap(fieldType));
        boolean isNativeType = jdbcType != null;
        if (jdbcType == null) {
            jdbcType = JdbcType.Int;  // ?
        }
        Column column = new Column(camelToSnake(fieldName), new ColumnType(jdbcType, null));

        return new SimpleTableField(field, getter, isPrimaryKey, null, isNativeType, column);
    }

    @VisibleForTesting
    static @Nullable Method findGetterMethod(@NotNull Field field) {
        Class<?> dataClass = field.getDeclaringClass();
        Class<?> fieldType = field.getType();
        String fieldName = field.getName().toLowerCase();
        return Arrays.stream(dataClass.getDeclaredMethods())
                .filter(method -> method.getReturnType() == fieldType &&
                                  method.getParameterCount() == 0 &&
                                  Modifier.isPublic(method.getModifiers()) &&
                                  !Modifier.isStatic(method.getModifiers()) &&
                                  method.getName().toLowerCase().contains(fieldName))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelToSnake(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelUpperToLower(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL).convert(s);
    }

    public record DataClassInput(@NotNull Class<?> dataClass, @NotNull String dataName) {
        public DataClassInput(@NotNull Class<?> dataClass) {
            this(dataClass, dataClass.getSimpleName());
        }
    }
}
