package io.webby.util.sql.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.mu.util.stream.BiStream;
import io.webby.util.EasyClasspath;
import io.webby.util.EasyClasspath.Scope;
import io.webby.util.EasyIterables;
import io.webby.util.EasyObjects;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;

import static io.webby.util.sql.schema.InvalidSqlModelException.failIf;
import static java.util.Objects.requireNonNull;

public class ModelSchemaFactory {
    private final ModelAdaptersLocator adaptersLocator;

    private final Iterable<ModelClassInput> inputs;
    private final Map<ModelClassInput, TableSchema> tables = new HashMap<>();
    private final Map<Class<?>, PojoSchema> pojos = new HashMap<>();

    public ModelSchemaFactory(@NotNull ModelAdaptersLocator adaptersLocator, @NotNull Iterable<ModelClassInput> inputs) {
        this.adaptersLocator = adaptersLocator;
        this.inputs = inputs;
    }

    public void build() {
        tables.clear();
        pojos.clear();

        for (ModelClassInput input : inputs) {
            tables.put(input, buildShallowTable(input));
        }
        for (Map.Entry<ModelClassInput, TableSchema> entry : tables.entrySet()) {
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
    @NotNull TableSchema buildShallowTable(@NotNull ModelClassInput input) {
        String sqlName = Naming.camelToSnake(input.modelName());
        String javaName = "%sTable".formatted(input.modelName());
        return new TableSchema(sqlName, javaName, input.modelName(), input.modelClass(), new ArrayList<>());
    }

    @VisibleForTesting
    void completeTable(@NotNull ModelClassInput input, @NotNull TableSchema schema) {
        List<TableField> fields = schema.fields();
        Arrays.stream(input.modelClass().getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> buildTableField(field, input.modelName()))
                .forEach(fields::add);
    }

    @VisibleForTesting
    @NotNull TableField buildTableField(@NotNull Field field, @NotNull String modelName) {
        Method getter = findGetterMethodOrDie(field);
        boolean isPrimaryKey = isPrimaryKeyField(field.getName(), modelName);

        FieldInference inference = inferFieldSchema(field);
        if (inference.isSingleColumn()) {
            return new SimpleTableField(ModelField.of(field, getter),
                                        isPrimaryKey,
                                        inference.foreignTable,
                                        inference.adapterInfo(),
                                        requireNonNull(inference.singleColumn));
        } else {
            return new MultiColumnTableField(ModelField.of(field, getter),
                                             isPrimaryKey,
                                             inference.foreignTable,
                                             requireNonNull(inference.adapterInfo()),
                                             requireNonNull(inference.multiColumns));
        }
    }

    private @NotNull FieldInference inferFieldSchema(@NotNull Field field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        String sqlFieldName = Naming.camelToSnake(fieldName);

        JdbcType jdbcType = JdbcType.findByMatchingNativeType(fieldType);
        if (jdbcType != null) {
            return FieldInference.ofNativeColumn(new Column(sqlFieldName, new ColumnType(jdbcType)));
        }

        Class<?> adapterClass = adaptersLocator.locateAdapterClass(fieldType);
        if (adapterClass != null) {
            Parameter[] parameters = getCreationParameters(adapterClass);
            if (parameters.length == 1) {
                JdbcType paramType = JdbcType.findByMatchingNativeType(parameters[0].getType());
                failIf(paramType == null, "JDBC adapter `%s` has incompatible parameters: %s", AdapterInfo.CREATE, adapterClass);
                Column column = new Column(sqlFieldName, new ColumnType(paramType));
                return FieldInference.ofSingleColumn(column, AdapterInfo.ofClass(adapterClass));
            } else {
                List<Column> columns = BiStream.from(Arrays.stream(parameters),
                                                     Parameter::getName,
                                                     param -> JdbcType.findByMatchingNativeType(param.getType()))
                        .mapKeys(name -> "%s_%s".formatted(sqlFieldName, Naming.camelToSnake(name)))
                        .mapValues(ColumnType::new)
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
                                         @Nullable ImmutableList<Column> multiColumns,
                                         @Nullable AdapterInfo adapterInfo,
                                         @Nullable TableSchema foreignTable) {
        public static FieldInference ofNativeColumn(@NotNull Column column) {
            return new FieldInference(column, null, null, null);
        }
        public static FieldInference ofSingleColumn(@NotNull Column column, @NotNull AdapterInfo adapterInfo) {
            return new FieldInference(column, null, adapterInfo, null);
        }

        public static FieldInference ofMultiColumns(@NotNull List<Column> columns, @NotNull AdapterInfo adapterInfo) {
            return new FieldInference(null, ImmutableList.copyOf(columns), adapterInfo, null);
        }

        public boolean isSingleColumn() {
            return singleColumn != null;
        }
    }

    @VisibleForTesting
    static @NotNull Parameter[] getCreationParameters(@NotNull Class<?> adapterClass) {
        Method method = EasyClasspath.findMethod(adapterClass, Scope.DECLARED, AdapterInfo.CREATE);
        failIf(method == null, "JDBC adapter does not implement `%s` method: %s", AdapterInfo.CREATE, adapterClass);
        return method.getParameters();
    }

    private @NotNull PojoSchema buildSchemaForPojoField(@NotNull Class<?> type) {
        return pojos.computeIfAbsent(type, k -> {
            // adaptersLocator.locateAdapterClass(type);

            if (type.isEnum()) {
                return new PojoSchema(type, ImmutableList.of(PojoField.ofEnum(type)));
            }

            ImmutableList<PojoField> pojoFields = Arrays.stream(type.getDeclaredFields())
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
            }).collect(ImmutableList.toImmutableList());
            return new PojoSchema(type, pojoFields);
        });
    }

    private static @NotNull Method findGetterMethodOrDie(@NotNull Field field) {
        Method getter = findGetterMethod(field);
        failIf(getter == null, "Model class `%s` exposes no getter field: %s",
               field.getDeclaringClass().getSimpleName(), field.getName());
        return getter;
    }

    @VisibleForTesting
    static @Nullable Method findGetterMethod(@NotNull Field field) {
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();

        Map<String, Method> eligibleMethods = Arrays.stream(field.getDeclaringClass().getMethods())
                .filter(method -> method.getReturnType() == fieldType &&
                                  method.getParameterCount() == 0 &&
                                  Modifier.isPublic(method.getModifiers()) &&
                                  !Modifier.isStatic(method.getModifiers()))
                .collect(ImmutableMap.toImmutableMap(Method::getName, Function.identity()));

        return EasyObjects.firstNonNull(List.of(
            () -> eligibleMethods.get(fieldName),
            () -> eligibleMethods.get("get%s".formatted(Naming.camelLowerToUpper(fieldName))),
            () -> EasyIterables.getOnlyItem(eligibleMethods.values().stream().filter(method -> {
                    String name = method.getName().toLowerCase();
                    return name.startsWith("get") && name.contains(fieldName.toLowerCase());
                })).orElse(null)
        ));
    }

    private static boolean isPrimaryKeyField(@NotNull String fieldName, @NotNull String modelName) {
        return fieldName.equals("id") || fieldName.equals(Naming.camelUpperToLower(modelName) + "Id");
    }

    private static void validateFieldForPojo(@NotNull Field field) {
        Class<?> modelClass = field.getDeclaringClass();
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        String typeName = fieldType.getSimpleName();

        failIf(fieldType.isInterface(), "Model class `%s` contains an interface field `%s` without a matching adapter: %s",
               modelClass, typeName, fieldName);
        failIf(Collection.class.isAssignableFrom(fieldType), "Model class `%s` contains a collection field: %s",
               modelClass, fieldName);
    }
}
