package io.webby.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.webby.orm.arch.Column;
import io.webby.orm.arch.ColumnType;
import io.webby.orm.arch.Naming;
import io.webby.orm.arch.factory.FieldResolver.ResolveResult;
import io.webby.orm.arch.model.*;
import io.webby.orm.codegen.ModelInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

import static java.util.Objects.requireNonNull;

class TableFieldArchFactory {
    private final TableArch table;
    private final Field field;
    private final ModelInput input;

    private final FieldResolver fieldResolver;
    private final RecursivePojoArchFactory pojoArchFactory;

    public TableFieldArchFactory(@NotNull RunContext runContext,
                                 @NotNull TableArch table,
                                 @NotNull Field field,
                                 @NotNull ModelInput input) {
        this.table = table;
        this.field = field;
        this.input = input;
        this.fieldResolver = new FieldResolver(runContext);
        this.pojoArchFactory = new RecursivePojoArchFactory(runContext);
    }

    public @NotNull TableField buildTableField() {
        ModelField modelField = JavaClassAnalyzer.toModelField(field);
        boolean isPrimaryKey = JavaClassAnalyzer.isPrimaryKeyField(field, input);
        boolean isUnique = JavaClassAnalyzer.isUniqueField(field);
        boolean isNullable = JavaClassAnalyzer.isNullableField(field);
        String[] defaults = JavaClassAnalyzer.getDefaults(field);

        FieldInference inference = inferFieldArch();
        if (inference.isForeignTable()) {
            return new ForeignTableField(table,
                                         modelField,
                                         Defaults.ofOneColumn(defaults),
                                         requireNonNull(inference.foreignTable()),
                                         requireNonNull(inference.singleColumn()));
        } else if (inference.isSingleColumn()) {
            return new OneColumnTableField(table,
                                           modelField,
                                           isPrimaryKey,
                                           isUnique,
                                           isNullable,
                                           Defaults.ofOneColumn(defaults),
                                           inference.adapterApi(),
                                           requireNonNull(inference.singleColumn()));
        } else {
            return new MultiColumnTableField(table,
                                             modelField,
                                             isPrimaryKey,
                                             isUnique,
                                             isNullable,
                                             Defaults.ofMultiColumns(inference.columnsNumber(), defaults),
                                             requireNonNull(inference.adapterApi()),
                                             requireNonNull(inference.multiColumns()));
        }
    }

    private @NotNull FieldInference inferFieldArch() {
        String fieldSqlName = Naming.fieldSqlName(field);

        ResolveResult resolved = fieldResolver.resolve(field);
        return switch (resolved.type()) {
            case NATIVE -> {
                Column column = new Column(fieldSqlName, new ColumnType(resolved.jdbcType()));
                yield FieldInference.ofNativeColumn(column);
            }
            case FOREIGN_KEY -> {
                String foreignIdSqlName = Naming.annotatedSqlName(field)
                    .orElseGet(() -> Naming.concatSqlNames(fieldSqlName, "id"));
                Column column = new Column(foreignIdSqlName, new ColumnType(resolved.foreignTable().second()));
                yield FieldInference.ofForeignKey(column, resolved.foreignTable().first());
            }
            case ADAPTER -> {
                AdapterApi adapterApi = AdapterApi.ofClass(resolved.adapterClass());
                List<Column> columns = adapterApi.adapterColumns(fieldSqlName);
                yield FieldInference.ofColumns(columns, adapterApi);
            }
            case POJO -> {
                PojoArch pojo = pojoArchFactory.buildPojoArchFor(field)
                    .reattachedTo(PojoParent.ofTerminal(field.getName(), fieldSqlName));
                yield FieldInference.ofColumns(pojo.columns(), AdapterApi.ofSignature(pojo));
            }
        };
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

        public int columnsNumber() {
            return multiColumns != null ? multiColumns.size() : 1;
        }
    }
}
