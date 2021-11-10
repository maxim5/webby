package io.webby.util.sql.schema;

import com.google.common.collect.ImmutableList;
import io.webby.util.lazy.AtomicLazyList;
import io.webby.util.sql.api.Foreign;
import io.webby.util.sql.api.ForeignInt;
import io.webby.util.sql.api.ForeignLong;
import io.webby.util.sql.codegen.ModelAdaptersLocator;
import io.webby.util.sql.codegen.ModelClassInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.webby.util.sql.schema.InvalidSqlModelException.assure;
import static io.webby.util.sql.schema.InvalidSqlModelException.failIf;
import static java.util.Objects.requireNonNull;

public class ModelSchemaFactory {
    private final ModelAdaptersLocator adaptersLocator;

    private final Iterable<ModelClassInput> inputs;
    private final Map<Class<?>, TableSchema> tables = new ConcurrentHashMap<>();
    private final Map<Class<?>, PojoSchema> pojos = new ConcurrentHashMap<>();

    public ModelSchemaFactory(@NotNull ModelAdaptersLocator adaptersLocator, @NotNull Iterable<ModelClassInput> inputs) {
        this.adaptersLocator = adaptersLocator;
        this.inputs = inputs;
    }

    public void build() {
        tables.clear();
        pojos.clear();

        for (ModelClassInput input : inputs) {
            tables.put(input.modelClass(), buildShallowTable(input));
        }
        for (ModelClassInput input : inputs) {
            completeTable(input, tables.get(input.modelClass()));
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
        return new TableSchema(sqlName, javaName, input.modelName(), input.modelClass(), AtomicLazyList.ofUninitializedList());
    }

    @VisibleForTesting
    void completeTable(@NotNull ModelClassInput input, @NotNull TableSchema schema) {
        ImmutableList<TableField> fields = JavaClassAnalyzer.getAllFieldsOrdered(input.modelClass()).stream()
                .map(field -> buildTableField(field, input.modelName()))
                .collect(ImmutableList.toImmutableList());
        schema.initializeOrDie(fields);
    }

    @VisibleForTesting
    @NotNull TableField buildTableField(@NotNull Field field, @NotNull String modelName) {
        Method getter = JavaClassAnalyzer.findGetterMethodOrDie(field);
        boolean isPrimaryKey = isPrimaryKeyField(field.getName(), modelName);

        FieldInference inference = inferFieldSchema(field);
        if (inference.isForeignTable()) {
            return new ForeignTableField(ModelField.of(field, getter), requireNonNull(inference.foreignTable()));
        } else if (inference.isSingleColumn()) {
            return new OneColumnTableField(ModelField.of(field, getter),
                                           isPrimaryKey,
                                           inference.adapterInfo(),
                                           requireNonNull(inference.singleColumn));
        } else {
            return new MultiColumnTableField(ModelField.of(field, getter),
                                             isPrimaryKey,
                                             requireNonNull(inference.adapterInfo()),
                                             requireNonNull(inference.multiColumns));
        }
    }

    private @NotNull FieldInference inferFieldSchema(@NotNull Field field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();

        JdbcType jdbcType = JdbcType.findByMatchingNativeType(fieldType);
        if (jdbcType != null) {
            return FieldInference.ofNativeColumn(new Column(Naming.camelToSnake(fieldName), new ColumnType(jdbcType)));
        }

        if (Foreign.class.isAssignableFrom(fieldType)) {
            assure(fieldType == ForeignInt.class || fieldType == ForeignLong.class || fieldType == Foreign.class,
                   "Invalid foreign key reference: `%s`", fieldType.getSimpleName());
            Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            Class<?> entityType = (Class<?>) actualTypeArguments[actualTypeArguments.length - 1];
            TableSchema foreignTable = tables.get(entityType);
            failIf(foreignTable == null,
                   "Foreign model `%s` referenced from `%s` model is missing in the input set for table generation",
                   entityType.getSimpleName(), fieldType.getSimpleName());
            return FieldInference.ofForeignKey(foreignTable);
        }

        Class<?> adapterClass = adaptersLocator.locateAdapterClass(fieldType);
        if (adapterClass != null) {
            AdapterInfo adapterInfo = AdapterInfo.ofClass(adapterClass);
            List<Column> columns = adapterInfo.adapterColumns(fieldName);
            return FieldInference.ofColumns(columns, adapterInfo);
        }

        PojoSchema pojo = buildSchemaForPojoField(field).reattachedTo(PojoParent.ofTerminal(fieldName));
        return FieldInference.ofColumns(pojo.columns(), AdapterInfo.ofSignature(pojo));
    }

    private record FieldInference(@Nullable Column singleColumn,
                                  @Nullable ImmutableList<Column> multiColumns,
                                  @Nullable AdapterInfo adapterInfo,
                                  @Nullable TableSchema foreignTable) {
        public static FieldInference ofNativeColumn(@NotNull Column column) {
            return new FieldInference(column, null, null, null);
        }

        public static FieldInference ofColumns(@NotNull List<Column> columns, @NotNull AdapterInfo adapterInfo) {
            return columns.size() == 1 ? ofSingleColumn(columns.get(0), adapterInfo) : ofMultiColumns(columns, adapterInfo);
        }

        public static FieldInference ofSingleColumn(@NotNull Column column, @NotNull AdapterInfo adapterInfo) {
            return new FieldInference(column, null, adapterInfo, null);
        }

        public static FieldInference ofMultiColumns(@NotNull List<Column> columns, @NotNull AdapterInfo adapterInfo) {
            return new FieldInference(null, ImmutableList.copyOf(columns), adapterInfo, null);
        }

        public static FieldInference ofForeignKey(@NotNull TableSchema foreignTable) {
            return new FieldInference(null, null, null, foreignTable);
        }

        public boolean isForeignTable() {
            return foreignTable != null;
        }

        public boolean isSingleColumn() {
            return singleColumn != null;
        }
    }

    private @NotNull PojoSchema buildSchemaForPojoField(@NotNull Field field) {
        Class<?> type = field.getType();
        PojoSchema pojo = pojos.get(type);
        if (pojo == null) {
            pojo = buildSchemaForPojoFieldImpl(field);
            pojos.put(type, pojo);
        }
        return pojo;
    }

    private @NotNull PojoSchema buildSchemaForPojoFieldImpl(@NotNull Field field) {
        validateFieldForPojo(field);
        Class<?> type = field.getType();

        if (type.isEnum()) {
            return new PojoSchema(type, ImmutableList.of(PojoFieldNative.ofEnum(type)));
        }

        ImmutableList<PojoField> pojoFields = JavaClassAnalyzer.getAllFieldsOrdered(type).stream().map(subField -> {
            Method getter = JavaClassAnalyzer.findGetterMethodOrDie(subField);
            ModelField modelField = ModelField.of(subField, getter);

            Class<?> subFieldType = subField.getType();
            JdbcType jdbcType = JdbcType.findByMatchingNativeType(subFieldType);
            if (jdbcType != null) {
                return PojoFieldNative.ofNative(modelField, jdbcType);
            }

            Class<?> adapterClass = adaptersLocator.locateAdapterClass(subFieldType);
            if (adapterClass != null) {
                return PojoFieldAdapter.ofAdapter(modelField, adapterClass);
            }

            PojoSchema nestedPojo = buildSchemaForPojoField(subField);
            return PojoFieldNested.ofNestedPojo(modelField, nestedPojo);
        }).collect(ImmutableList.toImmutableList());
        return new PojoSchema(type, pojoFields);
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
        failIf(fieldType.isArray(), "Model class `%s` contains an array field `%s` without a matching adapter: %s",
               modelClass, typeName, fieldName);
    }
}
