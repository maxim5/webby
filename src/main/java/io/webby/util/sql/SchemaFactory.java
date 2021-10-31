package io.webby.util.sql;

import com.google.common.base.CaseFormat;
import com.google.mu.util.stream.BiStream;
import io.webby.util.EasyClasspath;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class SchemaFactory {
    private final DataClassAdaptersLocator adaptersLocator;

    private final Collection<DataClassInput> inputs;
    private final Map<DataClassInput, TableSchema> schemas = new HashMap<>();

    public SchemaFactory(@NotNull DataClassAdaptersLocator adaptersLocator,
                         @NotNull Collection<DataClassInput> inputs) {
        this.adaptersLocator = adaptersLocator;
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
        Method getter = findGetterMethod(field);
        assert getter != null : "No getter found for field: %s".formatted(field);
        boolean isPrimaryKey = isPrimaryKeyField(field, dataName, field.getName());

        FieldInference inference = inferFieldSchema(field);
        if (inference.isSingleColumn()) {
            return new SimpleTableField(field, getter, isPrimaryKey,
                                        inference.foreignTable,
                                        inference.adapterInfo(),
                                        requireNonNull(inference.singleColumn));
        } else {
            return new MultiColumnTableField(field, getter, isPrimaryKey,
                                             inference.foreignTable,
                                             requireNonNull(inference.adapterInfo()),
                                             requireNonNull(inference.multiColumns));
        }
    }

    private static boolean isPrimaryKeyField(@NotNull Field field, @NotNull String dataName, String name) {
        return name.equals("id") || field.getName().equals(camelUpperToLower(dataName) + "Id");
    }

    private @NotNull FieldInference inferFieldSchema(@NotNull Field field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();

        JdbcType jdbcType = JdbcType.findByMatchingNativeType(fieldType);
        if (jdbcType != null) {
            return FieldInference.ofNativeColumn(new Column(camelToSnake(fieldName), new ColumnType(jdbcType, null)));
        }

        Class<?> adapterClass = adaptersLocator.locateAdapterClass(fieldType);
        if (adapterClass != null) {
            Parameter[] parameters = getCreationParameters(adapterClass);
            if (parameters.length == 1) {
                JdbcType paramType = JdbcType.findByMatchingNativeType(parameters[0].getType());
                assert paramType != null : "JDBC adapter `%s` has incompatible parameters: %s".formatted(AdapterInfo.CREATE, adapterClass);
                Column column = new Column(camelToSnake(fieldName), new ColumnType(paramType, null));
                return FieldInference.ofSingleColumn(column, adapterClass);
            } else {
                List<Column> columns = BiStream.from(Arrays.stream(parameters),
                                                     Parameter::getName,
                                                     param -> JdbcType.findByMatchingNativeType(param.getType()))
                        .mapKeys(name -> "%s_%s".formatted(camelToSnake(fieldName), camelToSnake(name)))
                        .mapValues(paramType -> new ColumnType(paramType, null))
                        .mapToObj(Column::new)
                        .toList();
                return FieldInference.ofMultiColumns(columns, adapterClass);
            }
        }

        //   enum,
        //   foreign data class,
        //   pojo class,

        // throw new UnsupportedOperationException("Adapter class not found for `%s`. Not implemented".formatted(field));
        Column column = new Column(camelToSnake(fieldName), new ColumnType(JdbcType.Int, null));
        AdapterSignature signature = new AdapterSignature(fieldType);
        return FieldInference.ofSingle(column, AdapterInfo.ofSignature(signature));
    }

    private static record FieldInference(@Nullable Column singleColumn,
                                         @Nullable List<Column> multiColumns,
                                         @Nullable AdapterInfo adapterInfo,
                                         @Nullable TableSchema foreignTable) {
        public static FieldInference ofNativeColumn(@NotNull Column column) {
            return new FieldInference(column, null, null, null);
        }

        public static FieldInference ofSingleColumn(@NotNull Column column, @Nullable Class<?> adapterClass) {
            return new FieldInference(column, null, AdapterInfo.ofClass(adapterClass), null);
        }

        public static FieldInference ofMultiColumns(@NotNull List<Column> columns, @Nullable Class<?> adapterClass) {
            return new FieldInference(null, columns, AdapterInfo.ofClass(adapterClass), null);
        }

        public static FieldInference ofSingle(@NotNull Column column, @NotNull AdapterInfo adapterInfo) {
            return new FieldInference(column, null, adapterInfo, null);
        }

        public boolean isSingleColumn() {
            return singleColumn != null;
        }
    }

    @VisibleForTesting
    static @NotNull Parameter[] getCreationParameters(@NotNull Class<?> adapterClass) {
        Method method = EasyClasspath.findMethod(adapterClass, AdapterInfo.CREATE);
        assert method != null : "JDBC adapter does not implement `%s` method: %s".formatted(AdapterInfo.CREATE, adapterClass);
        return method.getParameters();
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
