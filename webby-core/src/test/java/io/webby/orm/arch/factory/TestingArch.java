package io.webby.orm.arch.factory;

import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.arch.Column;
import io.webby.orm.arch.ColumnType;
import io.webby.orm.arch.JdbcType;
import io.webby.orm.arch.model.*;
import io.webby.orm.codegen.ModelInput;
import io.webby.orm.testing.FakeModelAdaptersScanner;
import io.webby.util.collect.ListBuilder;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static io.webby.orm.arch.factory.TestingArch.FieldConstraints.*;

public class TestingArch {
    public static @NotNull TableArch buildTableArch(@NotNull Class<?> model) {
        RunContext runContext = TestingArch.newRunContext(model);
        new ArchFactory(runContext).build();
        return runContext.tables().getTableOrDie(model);
    }

    public static @NotNull TableArch buildTableArch(@NotNull Class<?> model, @NotNull List<Class<?>> rest) {
        RunContext runContext = TestingArch.newRunContext(ListBuilder.concatOne(rest, model));
        new ArchFactory(runContext).build();
        return runContext.tables().getTableOrDie(model);
    }

    public static @NotNull RunContext newRunContext(@NotNull Class<?> @NotNull ... models) {
        return new RunContext(newRunInputs(List.of(models)), FakeModelAdaptersScanner.DEFAULT_SCANNER);
    }

    public static @NotNull RunContext newRunContext(@NotNull Collection<Class<?>> models) {
        return new RunContext(newRunInputs(models), FakeModelAdaptersScanner.DEFAULT_SCANNER);
    }

    private static @NotNull RunInputs newRunInputs(@NotNull Collection<Class<?>> models) {
        List<ModelInput> modelInputs = models.stream().map(ModelInput::of).toList();
        return new RunInputs(modelInputs);
    }

    public static @NotNull TableArchSubject assertThat(@NotNull TableArch tableArch) {
        return new TableArchSubject(tableArch);
    }

    public static @NotNull TableFieldSubject assertThat(@NotNull TableField tableField) {
        return new TableFieldSubject(tableField);
    }

    @CanIgnoreReturnValue
    public record TableArchSubject(@NotNull TableArch tableArch) {
        public @NotNull TableArchSubject hasTableName(@NotNull String sqlName,
                                                      @NotNull String packageName,
                                                      @NotNull String javaName) {
            Truth.assertThat(tableArch.sqlName()).isEqualTo(sqlName);
            Truth.assertThat(tableArch.packageName()).isEqualTo(packageName);
            Truth.assertThat(tableArch.javaName()).isEqualTo(javaName);
            return this;
        }

        public @NotNull TableArchSubject hasModel(@NotNull String modelName, @NotNull Class<?> modelClass) {
            Truth.assertThat(tableArch.modelName()).isEqualTo(modelName);
            Truth.assertThat(tableArch.modelClass()).isEqualTo(modelClass);
            return this;
        }

        public @NotNull TableArchSubject hasFields(@NotNull TableFieldsStatus status) {
            Truth.assertThat(tableArch.hasPrimaryKeyField()).isEqualTo(status.hasPK);
            Truth.assertThat(tableArch.isPrimaryKeyInt()).isEqualTo(status.hasIntPK);
            Truth.assertThat(tableArch.isPrimaryKeyLong()).isEqualTo(status.hasLongPK);
            Truth.assertThat(tableArch.hasForeignKeyField()).isEqualTo(status.hasFK);
            return this;
        }

        public @NotNull TableFieldSubject hasSingleFieldThat(@NotNull String name) {
            Truth.assertThat(tableArch.fields()).hasSize(1);
            return hasFieldThat(name);
        }

        public @NotNull TableFieldSubject hasFieldThat(@NotNull String name) {
            TableField tableField = tableArch.fields().stream()
                .filter(field -> field.javaName().equals(name))
                .findAny()
                .orElseThrow();
            return assertThat(tableField);
        }
    }

    public enum TableFieldsStatus {
        HAS_PRIMARY(true, false, false, false),
        HAS_PRIMARY_INT(true, true, false, false),
        HAS_PRIMARY_LONG(true, false, true, false),
        HAS_FOREIGN(false, false, false, true),
        HAS_NO_KEY_FIELDS(false, false, false, false);

        private final boolean hasPK, hasIntPK, hasLongPK, hasFK;

        TableFieldsStatus(boolean hasPK, boolean hasIntPK, boolean hasLongPK, boolean hasFK) {
            this.hasPK = hasPK;
            this.hasIntPK = hasIntPK;
            this.hasLongPK = hasLongPK;
            this.hasFK = hasFK;
        }
    }

    @CanIgnoreReturnValue
    public record TableFieldSubject(@NotNull TableField tableField) {
        public @NotNull TableFieldSubject isFromTable(@NotNull String table) {
            Truth.assertThat(tableField.parent().sqlName()).isEqualTo(table);
            return this;
        }

        public @NotNull TableFieldSubject hasInJava(@NotNull Class<?> javaType, @NotNull String accessor) {
            Truth.assertThat(tableField.javaType()).isEqualTo(javaType);
            Truth.assertThat(tableField.javaAccessor()).isEqualTo(accessor);
            return this;
        }

        public @NotNull TableFieldSubject hasSize(int size) {
            Truth.assertThat(tableField.columns().size()).isEqualTo(size);
            Truth.assertThat(tableField.columnsNumber()).isEqualTo(size);
            Truth.assertThat(tableField.isSingleColumn()).isEqualTo(size == 1);
            Truth.assertThat(tableField.isMultiColumn()).isEqualTo(size > 1);
            Truth.assertThat(tableField instanceof MultiColumnTableField).isEqualTo(size > 1);
            Truth.assertThat(tableField instanceof OneColumnTableField || tableField instanceof ForeignTableField)
                .isEqualTo(size == 1);
            return this;
        }

        public @NotNull TableFieldSubject hasColumns(@NotNull String @NotNull ... columns) {
            List<String> names = tableField.columns().stream().map(Column::sqlName).toList();
            Truth.assertThat(names).containsExactly((Object[]) columns).inOrder();
            return hasSize(columns.length);
        }

        public @NotNull TableFieldSubject hasColumnTypes(@NotNull JdbcType @NotNull ... types) {
            List<JdbcType> jdbcTypes = tableField.columns().stream().map(Column::type).map(ColumnType::jdbcType).toList();
            Truth.assertThat(jdbcTypes).containsExactly((Object[]) types).inOrder();
            return hasSize(types.length);
        }

        @SafeVarargs
        public final @NotNull TableFieldSubject hasColumns(@NotNull Pair<String, JdbcType> @NotNull ... columns) {
            List<Pair<String, JdbcType>> typedColumns = tableField.columns().stream()
                .map(column -> Pair.of(column.sqlName(), column.type().jdbcType()))
                .toList();
            Truth.assertThat(typedColumns).containsExactly((Object[]) columns).inOrder();
            return hasSize(columns.length);
        }

        public @NotNull TableFieldSubject isSingleColumn(@NotNull String column, @NotNull JdbcType type) {
            return hasColumns(Pair.of(column, type));
        }

        public @NotNull TableFieldSubject hasConstraints(@NotNull FieldConstraints constraints) {
            Truth.assertThat(tableField.isPrimaryKey()).isEqualTo(constraints == PRIMARY_NOT_NULL);
            Truth.assertThat(tableField.isUnique()).isEqualTo(constraints == UNIQUE_NOT_NULL);
            Truth.assertThat(tableField.isForeignKey()).isEqualTo(constraints == FOREIGN_KEY_NOT_NULL);
            Truth.assertThat(tableField instanceof ForeignTableField).isEqualTo(tableField.isForeignKey());
            return this;
        }

        public @NotNull TableFieldSubject isForeign(@NotNull String column, @NotNull String table) {
            Truth.assertThat(tableField.isForeignKey()).isTrue();
            Truth.assertThat(((ForeignTableField) tableField).foreignKeyColumn().sqlName()).isEqualTo(column);
            Truth.assertThat(((ForeignTableField) tableField).getForeignTable()).isNotNull();
            Truth.assertThat(((ForeignTableField) tableField).getForeignTable().sqlName()).isEqualTo(table);
            Truth.assertThat(tableField.typeSupport()).isEqualTo(TableField.TypeSupport.FOREIGN_KEY);
            return this;
        }

        public @NotNull TableFieldSubject hasDefault(@NotNull String column, @Nullable String value) {
            Column col = tableField.columns().stream().filter(c -> c.sqlName().equals(column)).collect(onlyElement());
            Truth.assertThat(tableField.columnDefault(col)).isEqualTo(value);
            return this;
        }

        public @NotNull TableFieldSubject doesNotHaveAnyDefaults() {
            tableField.columns().forEach(column -> Truth.assertThat(tableField.columnDefault(column)).isNull());
            return this;
        }

        public @NotNull TableFieldSubject hasDefault(@Nullable String value) {
            hasSize(1);
            Truth.assertThat(tableField.columnDefault(tableField.columns().get(0))).isEqualTo(value);
            return this;
        }

        public @NotNull TableFieldSubject doesNotHaveDefault() {
            return hasDefault(null);
        }

        public @NotNull TableFieldSubject isNativelySupportedType() {
            Truth.assertThat(tableField.typeSupport()).isEqualTo(TableField.TypeSupport.NATIVE);
            Truth.assertThat(tableField.isNativelySupportedType()).isTrue();
            Truth.assertThat(tableField.isMapperSupportedType()).isFalse();
            Truth.assertThat(tableField.isAdapterSupportType()).isFalse();
            return this;
        }

        public @NotNull TableFieldSubject isAdapterSupportedType() {
            Truth.assertThat(tableField.typeSupport()).isEqualTo(TableField.TypeSupport.ADAPTER_API);
            Truth.assertThat(tableField.isAdapterSupportType()).isTrue();
            Truth.assertThat(tableField.isNativelySupportedType()).isFalse();
            Truth.assertThat(tableField.isMapperSupportedType()).isFalse();
            return this;
        }

        public @NotNull TableFieldSubject usesAdapter(@NotNull String staticRef) {
            Truth.assertThat(tableField.adapterApiOrDie().staticRef()).isEqualTo(staticRef);
            return this;
        }
    }

    public enum FieldConstraints {
        PRIMARY_NOT_NULL,
        UNIQUE_NOT_NULL,
        FOREIGN_KEY_NOT_NULL,
        USUAL_NOT_NULL,
    }
}
