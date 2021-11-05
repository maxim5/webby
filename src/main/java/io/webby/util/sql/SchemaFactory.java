package io.webby.util.sql;

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

import static io.webby.util.sql.schema.InvalidSqlModelException.failIf;
import static java.util.Objects.requireNonNull;

public class SchemaFactory {
    private final DataClassAdaptersLocator adaptersLocator;

    private final Collection<DataClassInput> inputs;
    private final Map<DataClassInput, TableSchema> tables = new HashMap<>();
    private final Map<Class<?>, PojoSchema> pojos = new HashMap<>();

    public SchemaFactory(@NotNull DataClassAdaptersLocator adaptersLocator,
                         @NotNull Collection<DataClassInput> inputs) {
        this.adaptersLocator = adaptersLocator;
        this.inputs = inputs;
    }

    public void build() {
        tables.clear();
        pojos.clear();

        for (DataClassInput input : inputs) {
            tables.put(input, buildShallowTable(input));
        }
        for (Map.Entry<DataClassInput, TableSchema> entry : tables.entrySet()) {
            completeTable(entry.getKey(), entry.getValue());
        }
    }

    public @NotNull Collection<TableSchema> getTableSchemas() {
        return tables.values();
    }

    public @NotNull Collection<AdapterSchema> getAdapterSchemas() {
        return pojos.values().stream().map(AdapterSchema::new).toList();
    }

    @VisibleForTesting
    @NotNull TableSchema buildShallowTable(@NotNull DataClassInput input) {
        String sqlName = Naming.camelToSnake(input.dataName);
        String javaName = "%sTable".formatted(input.dataName);
        return new TableSchema(sqlName, javaName, input.dataName, input.dataClass, new ArrayList<>());
    }

    @VisibleForTesting
    void completeTable(@NotNull DataClassInput input, @NotNull TableSchema schema) {
        List<TableField> fields = schema.fields();
        Arrays.stream(input.dataClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> buildTableField(field, input.dataName))
                .forEach(fields::add);
    }

    @VisibleForTesting
    @NotNull TableField buildTableField(@NotNull Field field, @NotNull String dataName) {
        Method getter = findGetterMethodOrDie(field);
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
        return name.equals("id") || field.getName().equals(Naming.camelUpperToLower(dataName) + "Id");
    }

    private @NotNull FieldInference inferFieldSchema(@NotNull Field field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        String sqlFieldName = Naming.camelToSnake(fieldName);

        JdbcType jdbcType = JdbcType.findByMatchingNativeType(fieldType);
        if (jdbcType != null) {
            return FieldInference.ofNativeColumn(new Column(sqlFieldName, new ColumnType(jdbcType, null)));
        }

        Class<?> adapterClass = adaptersLocator.locateAdapterClass(fieldType);
        if (adapterClass != null) {
            Parameter[] parameters = getCreationParameters(adapterClass);
            if (parameters.length == 1) {
                JdbcType paramType = JdbcType.findByMatchingNativeType(parameters[0].getType());
                failIf(paramType == null, "JDBC adapter `%s` has incompatible parameters: %s", AdapterInfo.CREATE, adapterClass);
                Column column = new Column(sqlFieldName, new ColumnType(paramType, null));
                return FieldInference.ofSingleColumn(column, AdapterInfo.ofClass(adapterClass));
            } else {
                List<Column> columns = BiStream.from(Arrays.stream(parameters),
                                                     Parameter::getName,
                                                     param -> JdbcType.findByMatchingNativeType(param.getType()))
                        .mapKeys(name -> "%s_%s".formatted(sqlFieldName, Naming.camelToSnake(name)))
                        .mapValues(paramType -> new ColumnType(paramType, null))
                        .mapToObj(Column::new)
                        .toList();
                return FieldInference.ofMultiColumns(columns, AdapterInfo.ofClass(adapterClass));
            }
        }

        validateFieldForPojo(field);

        PojoSchema pojoSchema = buildSchemaForPojoField(fieldType);
        List<Column> columns = pojoSchema.columns().stream()
                .map(column -> column.renamed("%s_%s".formatted(sqlFieldName, column.sqlName())))
                .toList();
        if (columns.size() == 1) {
            return FieldInference.ofSingleColumn(columns.get(0), AdapterInfo.ofSignature(pojoSchema));
        } else {
            return FieldInference.ofMultiColumns(columns, AdapterInfo.ofSignature(pojoSchema));
        }
    }

    private static record FieldInference(@Nullable Column singleColumn,
                                         @Nullable List<Column> multiColumns,
                                         @Nullable AdapterInfo adapterInfo,
                                         @Nullable TableSchema foreignTable) {
        public static FieldInference ofNativeColumn(@NotNull Column column) {
            return new FieldInference(column, null, null, null);
        }
        public static FieldInference ofSingleColumn(@NotNull Column column, @NotNull AdapterInfo adapterInfo) {
            return new FieldInference(column, null, adapterInfo, null);
        }

        public static FieldInference ofMultiColumns(@NotNull List<Column> columns, @NotNull AdapterInfo adapterInfo) {
            return new FieldInference(null, columns, adapterInfo, null);
        }

        public boolean isSingleColumn() {
            return singleColumn != null;
        }
    }

    @VisibleForTesting
    static @NotNull Parameter[] getCreationParameters(@NotNull Class<?> adapterClass) {
        Method method = EasyClasspath.findMethod(adapterClass, AdapterInfo.CREATE);
        failIf(method == null, "JDBC adapter does not implement `%s` method: %s", AdapterInfo.CREATE, adapterClass);
        return method.getParameters();
    }

    private @NotNull PojoSchema buildSchemaForPojoField(@NotNull Class<?> type) {
        return pojos.computeIfAbsent(type, k -> {
            // adaptersLocator.locateAdapterClass(type);

            if (type.isEnum()) {
                return new PojoSchema(type, Collections.emptyList());
            }

            List<PojoField> pojoFields = Arrays.stream(type.getDeclaredFields())
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    .map(field -> {
                validateFieldForPojo(field);
                Method getter = findGetterMethodOrDie(field);

                Class<?> subFieldType = field.getType();
                JdbcType jdbcType = JdbcType.findByMatchingNativeType(subFieldType);
                if (jdbcType != null) {
                    return PojoField.ofNative(field, getter, jdbcType);
                } else {
                    PojoSchema subField = buildSchemaForPojoField(subFieldType);
                    return PojoField.ofSubPojo(field, getter, subField);
                }
            }).toList();
            return new PojoSchema(type, pojoFields);
        });
    }

    @VisibleForTesting
    static @NotNull Method findGetterMethodOrDie(@NotNull Field field) {
        Method getter = findGetterMethod(field);
        failIf(getter == null, "Model class `%s` exposes no getter field: %s", field.getDeclaringClass(), field.getName());
        return getter;
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

    public record DataClassInput(@NotNull Class<?> dataClass, @NotNull String dataName) {
        public DataClassInput(@NotNull Class<?> dataClass) {
            this(dataClass, Naming.generatedSimpleName(dataClass));
        }
    }

    private static void validateFieldForPojo(@NotNull Field field) {
        Class<?> type = field.getType();
        Class<?> klass = field.getDeclaringClass();
        String name = field.getName();
        String typeName = type.getSimpleName();

        failIf(type.isInterface(), "Model class `%s` contains an interface field `%s` without a matching adapter: %s",
               klass, typeName, name);
        failIf(Collection.class.isAssignableFrom(type), "Model class `%s` contains a collection field: %s", klass, name);
    }
}
