package io.webby.orm.arch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.webby.util.collect.Pair;
import io.webby.util.lazy.AtomicLazyList;
import io.webby.orm.api.Foreign;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.ForeignObj;
import io.webby.orm.codegen.ModelAdaptersScanner;
import io.webby.orm.codegen.ModelInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static io.webby.orm.arch.InvalidSqlModelException.assure;
import static io.webby.orm.arch.InvalidSqlModelException.failIf;
import static java.util.Objects.requireNonNull;

public class ArchFactory {
    private final ModelAdaptersScanner adaptersScanner;

    private final Iterable<ModelInput> inputs;
    private final Map<Class<?>, TableArch> tables = new LinkedHashMap<>();
    private final Map<Class<?>, PojoArch> pojos = new LinkedHashMap<>();

    public ArchFactory(@NotNull ModelAdaptersScanner scanner, @NotNull Iterable<ModelInput> inputs) {
        this.adaptersScanner = scanner;
        this.inputs = inputs;
    }

    public void build() {
        tables.clear();
        pojos.clear();

        for (ModelInput input : inputs) {
            TableArch table = buildShallowTable(input);
            input.keys().forEach(key -> assure(tables.put(key, table) == null, "Duplicate input: " + key));
        }
        for (ModelInput input : inputs) {
            completeTable(input, tables.get(input.modelClass()));
        }
    }

    public @NotNull ImmutableMap<Class<?>, TableArch> getAllTables() {
        return ImmutableMap.copyOf(tables);
    }

    public @NotNull Collection<TableArch> getTableArches() {
        return tables.values();
    }

    public @NotNull Collection<AdapterArch> getAdapterArches() {
        return pojos.values().stream().map(AdapterArch::new).toList();
    }

    @VisibleForTesting
    @NotNull TableArch buildShallowTable(@NotNull ModelInput input) {
        return new TableArch(input.sqlName(), input.javaTableName(), input.modelClass(), AtomicLazyList.ofUninitializedList());
    }

    @VisibleForTesting
    void completeTable(@NotNull ModelInput input, @NotNull TableArch table) {
        ImmutableList<TableField> fields = JavaClassAnalyzer.getAllFieldsOrdered(input.modelClass()).stream()
                .map(field -> buildTableField(table, field, input))
                .collect(ImmutableList.toImmutableList());
        table.initializeOrDie(fields);
    }

    @VisibleForTesting
    @NotNull TableField buildTableField(@NotNull TableArch table, @NotNull Field field, @NotNull ModelInput input) {
        Method getter = JavaClassAnalyzer.findGetterMethodOrDie(field);
        boolean isPrimaryKey = isPrimaryKeyField(field.getName(), input);

        FieldInference inference = inferFieldArch(field);
        if (inference.isForeignTable()) {
            return new ForeignTableField(table,
                                         ModelField.of(field, getter),
                                         requireNonNull(inference.foreignTable()),
                                         requireNonNull(inference.singleColumn()));
        } else if (inference.isSingleColumn()) {
            return new OneColumnTableField(table,
                                           ModelField.of(field, getter),
                                           isPrimaryKey,
                                           inference.adapterApi(),
                                           requireNonNull(inference.singleColumn()));
        } else {
            return new MultiColumnTableField(table,
                                             ModelField.of(field, getter),
                                             isPrimaryKey,
                                             requireNonNull(inference.adapterApi()),
                                             requireNonNull(inference.multiColumns()));
        }
    }

    private @NotNull FieldInference inferFieldArch(@NotNull Field field) {
        String fieldName = field.getName();
        String fieldSqlName = Naming.fieldSqlName(field);
        Class<?> fieldType = field.getType();

        JdbcType jdbcType = JdbcType.findByMatchingNativeType(fieldType);
        if (jdbcType != null) {
            return FieldInference.ofNativeColumn(new Column(fieldSqlName, new ColumnType(jdbcType)));
        }

        Pair<TableArch, JdbcType> foreignTableInfo = findForeignTableInfo(field);
        if (foreignTableInfo != null) {
            String foreignIdSqlName = Naming.annotatedSqlName(field).orElseGet(() -> Naming.concatSqlNames(fieldSqlName, "id"));
            Column column = new Column(foreignIdSqlName, new ColumnType(foreignTableInfo.second()));
            return FieldInference.ofForeignKey(column, foreignTableInfo.first());
        }

        Class<?> adapterClass = adaptersScanner.locateAdapterClass(fieldType);
        if (adapterClass != null) {
            AdapterApi adapterApi = AdapterApi.ofClass(adapterClass);
            List<Column> columns = adapterApi.adapterColumns(fieldSqlName);
            return FieldInference.ofColumns(columns, adapterApi);
        }

        PojoArch pojo = buildArchForPojoField(field).reattachedTo(PojoParent.ofTerminal(fieldName, fieldSqlName));
        return FieldInference.ofColumns(pojo.columns(), AdapterApi.ofSignature(pojo));
    }

    private record FieldInference(@Nullable Column singleColumn,
                                  @Nullable ImmutableList<Column> multiColumns,
                                  @Nullable AdapterApi adapterApi,
                                  @Nullable TableArch foreignTable) {
        public static FieldInference ofNativeColumn(@NotNull Column column) {
            return new FieldInference(column, null, null, null);
        }

        public static FieldInference ofColumns(@NotNull List<Column> columns, @NotNull AdapterApi adapterApi) {
            return columns.size() == 1 ? ofSingleColumn(columns.get(0), adapterApi) : ofMultiColumns(columns, adapterApi);
        }

        public static FieldInference ofSingleColumn(@NotNull Column column, @NotNull AdapterApi adapterApi) {
            return new FieldInference(column, null, adapterApi, null);
        }

        public static FieldInference ofMultiColumns(@NotNull List<Column> columns, @NotNull AdapterApi adapterApi) {
            return new FieldInference(null, ImmutableList.copyOf(columns), adapterApi, null);
        }

        public static FieldInference ofForeignKey(@NotNull Column column, @NotNull TableArch foreignTable) {
            return new FieldInference(column, null, null, foreignTable);
        }

        public boolean isForeignTable() {
            return foreignTable != null;
        }

        public boolean isSingleColumn() {
            return singleColumn != null;
        }
    }

    private @Nullable Pair<TableArch, JdbcType> findForeignTableInfo(@NotNull Field field) {
        Class<?> fieldType = field.getType();
        if (!Foreign.class.isAssignableFrom(fieldType)) {
            return null;
        }

        assure(fieldType == Foreign.class || fieldType == ForeignInt.class ||
               fieldType == ForeignLong.class || fieldType == ForeignObj.class,
               "Invalid foreign key reference type: `%s`. Supported types: Foreign, ForeignInt, ForeignLong, ForeignObj",
               fieldType.getSimpleName());
        Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        Class<?> keyType =
                fieldType == ForeignInt.class ?
                        int.class :
                fieldType == ForeignLong.class ?
                        long.class :
                        (Class<?>) actualTypeArguments[0];
        Class<?> entityType = (Class<?>) actualTypeArguments[actualTypeArguments.length - 1];

        TableArch foreignTable = tables.get(entityType);
        failIf(foreignTable == null,
               "Foreign model `%s` referenced from `%s` model is missing in the input set for table generation",
               entityType.getSimpleName(), fieldType.getSimpleName());
        assure(foreignTable.hasPrimaryKeyField(),
               "Foreign model `%s` does not have a primary key. Expected key type: `%s`",
               entityType.getSimpleName(), keyType);
        Class<?> primaryKeyType = requireNonNull(foreignTable.primaryKeyField()).javaType();
        assure(primaryKeyType == keyType,
               "Foreign model `%s` primary key `%s` does match the foreign key",
               entityType.getSimpleName(), primaryKeyType, keyType);

        JdbcType jdbcType = JdbcType.findByMatchingNativeType(primaryKeyType);
        failIf(jdbcType == null,
               "Foreign model `%s` primary key `%s` must be natively supported type (i.e., primitive or String)",
               entityType.getSimpleName(), primaryKeyType, keyType);

        return Pair.of(foreignTable, jdbcType);
    }

    private @NotNull PojoArch buildArchForPojoField(@NotNull Field field) {
        Class<?> type = field.getType();
        PojoArch pojo = pojos.get(type);
        if (pojo == null) {
            pojo = buildArchForPojoFieldImpl(field);
            pojos.put(type, pojo);
        }
        return pojo;
    }

    private @NotNull PojoArch buildArchForPojoFieldImpl(@NotNull Field field) {
        validateFieldForPojo(field);
        Class<?> type = field.getType();

        if (type.isEnum()) {
            return new PojoArch(type, ImmutableList.of(PojoFieldNative.ofEnum(type)));
        }

        ImmutableList<PojoField> pojoFields = JavaClassAnalyzer.getAllFieldsOrdered(type).stream().map(subField -> {
            Method getter = JavaClassAnalyzer.findGetterMethodOrDie(subField);
            ModelField modelField = ModelField.of(subField, getter);

            Class<?> subFieldType = subField.getType();
            JdbcType jdbcType = JdbcType.findByMatchingNativeType(subFieldType);
            if (jdbcType != null) {
                return PojoFieldNative.ofNative(modelField, jdbcType);
            }

            Class<?> adapterClass = adaptersScanner.locateAdapterClass(subFieldType);
            if (adapterClass != null) {
                return PojoFieldAdapter.ofAdapter(modelField, adapterClass);
            }

            PojoArch nestedPojo = buildArchForPojoField(subField);
            return PojoFieldNested.ofNestedPojo(modelField, nestedPojo);
        }).collect(ImmutableList.toImmutableList());
        return new PojoArch(type, pojoFields);
    }

    private static boolean isPrimaryKeyField(@NotNull String fieldName, @NotNull ModelInput input) {
        return fieldName.equals("id") ||
               fieldName.equals(Naming.idJavaName(input.modelClass())) ||
               (input.modelInterface() != null && fieldName.equals(Naming.idJavaName(input.modelInterface())));
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
