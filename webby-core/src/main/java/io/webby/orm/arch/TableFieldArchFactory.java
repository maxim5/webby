package io.webby.orm.arch;

import com.google.common.collect.ImmutableList;
import io.webby.orm.api.Foreign;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.ForeignObj;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.codegen.ModelInput;
import io.webby.util.collect.Pair;
import io.webby.util.reflect.EasyAnnotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.webby.orm.arch.InvalidSqlModelException.assure;
import static io.webby.orm.arch.InvalidSqlModelException.failIf;
import static java.util.Objects.requireNonNull;

class TableFieldArchFactory {
    private final RunContext runContext;

    private final TableArch table;
    private final Field field;
    private final ModelInput input;

    public TableFieldArchFactory(@NotNull RunContext runContext,
                                 @NotNull TableArch table,
                                 @NotNull Field field,
                                 @NotNull ModelInput input) {
        this.runContext = runContext;
        this.table = table;
        this.field = field;
        this.input = input;
    }

    @VisibleForTesting
    @NotNull TableField buildTableField() {
        Method getter = JavaClassAnalyzer.findGetterMethodOrDie(field);
        boolean isPrimaryKey = isPrimaryKeyField(field, input);
        boolean isUnique = isUniqueField(field);

        FieldInference inference = inferFieldArch();
        if (inference.isForeignTable()) {
            return new ForeignTableField(table,
                                         ModelField.of(field, getter),
                                         requireNonNull(inference.foreignTable()),
                                         requireNonNull(inference.singleColumn()));
        } else if (inference.isSingleColumn()) {
            return new OneColumnTableField(table,
                                           ModelField.of(field, getter),
                                           isPrimaryKey,
                                           isUnique,
                                           inference.adapterApi(),
                                           requireNonNull(inference.singleColumn()));
        } else {
            return new MultiColumnTableField(table,
                                             ModelField.of(field, getter),
                                             isPrimaryKey,
                                             isUnique,
                                             requireNonNull(inference.adapterApi()),
                                             requireNonNull(inference.multiColumns()));
        }
    }

    private @NotNull FieldInference inferFieldArch() {
        String fieldName = field.getName();
        String fieldSqlName = Naming.fieldSqlName(field);
        Class<?> fieldType = field.getType();

        JdbcType jdbcType = JdbcType.findByMatchingNativeType(fieldType);
        if (jdbcType != null) {
            return FieldInference.ofNativeColumn(new Column(fieldSqlName, new ColumnType(jdbcType)));
        }

        Pair<TableArch, JdbcType> foreignTableInfo = findForeignTableInfo();
        if (foreignTableInfo != null) {
            String foreignIdSqlName = Naming.annotatedSqlName(field).orElseGet(() -> Naming.concatSqlNames(fieldSqlName, "id"));
            Column column = new Column(foreignIdSqlName, new ColumnType(foreignTableInfo.second()));
            return FieldInference.ofForeignKey(column, foreignTableInfo.first());
        }

        Class<?> adapterClass = runContext.adaptersScanner().locateAdapterClass(fieldType);
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

    private @Nullable Pair<TableArch, JdbcType> findForeignTableInfo() {
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

        TableArch foreignTable = runContext.tables().getTable(entityType);
        failIf(foreignTable == null,
               "Foreign model `%s` referenced from `%s` model is missing in the input set for table generation",
               entityType.getSimpleName(), field.getDeclaringClass().getSimpleName());

        Class<?> primaryKeyType;
        if (foreignTable.isInitialized()) {
            assure(foreignTable.hasPrimaryKeyField(),
                   "Foreign model `%s` does not have a primary key. Expected key type: `%s`",
                   entityType.getSimpleName(), keyType);
            primaryKeyType = requireNonNull(foreignTable.primaryKeyField()).javaType();
        } else {
            // A hacky way to get the PK field for a foreign model that hasn't been built yet.
            // Essentially, this duplicates the work of `buildTableField` above.
            // In theory, should work for dependency cycles.
            Class<?> foreignModelClass = field.getDeclaringClass();
            ModelInput foreignInput = runContext.inputs().findInputByModel(foreignModelClass).orElseThrow();
            Optional<Field> foreignPrimaryKeyField = JavaClassAnalyzer.getAllFieldsOrdered(foreignModelClass).stream()
                .filter(f -> isPrimaryKeyField(f, foreignInput))
                .findFirst();
            assure(foreignPrimaryKeyField.isPresent(),
                   "Foreign model `%s` does not have a primary key. Expected key type: `%s`",
                   entityType.getSimpleName(), keyType);
            primaryKeyType = foreignPrimaryKeyField.get().getType();
        }

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
        return runContext.pojos().getOrCompute(field.getType(), () -> buildArchForPojoFieldImpl(field));
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

            Class<?> adapterClass = runContext.adaptersScanner().locateAdapterClass(subFieldType);
            if (adapterClass != null) {
                return PojoFieldAdapter.ofAdapter(modelField, adapterClass);
            }

            PojoArch nestedPojo = buildArchForPojoField(subField);
            return PojoFieldNested.ofNestedPojo(modelField, nestedPojo);
        }).collect(ImmutableList.toImmutableList());
        return new PojoArch(type, pojoFields);
    }

    private static boolean isPrimaryKeyField(@NotNull Field field, @NotNull ModelInput input) {
        String fieldName = field.getName();
        return fieldName.equals("id") ||
            fieldName.equals(Naming.idJavaName(input.javaModelName())) ||
            fieldName.equals(Naming.idJavaName(input.modelClass())) ||
            (input.modelInterface() != null && fieldName.equals(Naming.idJavaName(input.modelInterface()))) ||
            EasyAnnotations.getOptionalAnnotation(field, Sql.class).map(Sql::primary).orElse(false);
    }

    private static boolean isUniqueField(@NotNull Field field) {
        return EasyAnnotations.getOptionalAnnotation(field, Sql.class).map(Sql::unique).orElse(false);
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
        failIf(fieldType == Object.class, "Model class `%s` contains a `%s` field: %s",
               modelClass, typeName, fieldName);
    }
}
