package io.webby.orm.arch;

import com.google.common.collect.ImmutableList;
import io.webby.orm.codegen.ModelInput;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Objects.requireNonNull;

class TableFieldArchFactory {
    private final RunContext runContext;

    private final TableArch table;
    private final Field field;
    private final ModelInput input;

    private final RecursivePojoArchFactory pojoArchFactory;
    private final ForeignTableArchResolver foreignTableArchResolver;

    public TableFieldArchFactory(@NotNull RunContext runContext,
                                 @NotNull TableArch table,
                                 @NotNull Field field,
                                 @NotNull ModelInput input) {
        this.runContext = runContext;
        this.table = table;
        this.field = field;
        this.input = input;
        this.pojoArchFactory = new RecursivePojoArchFactory(runContext);
        this.foreignTableArchResolver = new ForeignTableArchResolver(runContext);
    }

    @VisibleForTesting
    @NotNull TableField buildTableField() {
        Method getter = JavaClassAnalyzer.findGetterMethodOrDie(field);
        boolean isPrimaryKey = JavaClassAnalyzer.isPrimaryKeyField(field, input);
        boolean isUnique = JavaClassAnalyzer.isUniqueField(field);

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

        Pair<TableArch, JdbcType> foreignTableInfo = foreignTableArchResolver.findForeignTableInfo(field);
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

        PojoArch pojo = pojoArchFactory.buildPojoArchFor(field)
            .reattachedTo(PojoParent.ofTerminal(fieldName, fieldSqlName));
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
}
