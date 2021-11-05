package io.webby.util.sql;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import io.webby.util.EasyMaps;
import io.webby.util.func.ObjIntBiFunction;
import io.webby.util.sql.api.*;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.webby.util.sql.schema.ColumnJoins.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnnecessaryStringEscape")
public class ModelTableCodegen extends BaseCodegen {
    private final ModelAdaptersLocator adaptersLocator;
    private final TableSchema table;

    private final Map<String, String> mainContext;
    private final Map<String, String> pkContext;

    public ModelTableCodegen(@NotNull ModelAdaptersLocator adaptersLocator,
                             @NotNull TableSchema table, @NotNull Appendable writer) {
        super(writer);
        this.adaptersLocator = adaptersLocator;
        this.table = table;

        this.mainContext = EasyMaps.asMap(
            "$TableClass", table.javaName(),
            "$table_sql", table.sqlName(),
            "$ModelClass", Naming.shortCanonicalName(table.modelClass()),
            "$model_param", table.modelName().toLowerCase(),
            "$all_columns", joinWithComma(table.columns())
        );

        TableField primaryKeyField = table.primaryKeyField();
        this.pkContext = primaryKeyField == null ? Map.of() : EasyMaps.asMap(
            "$pk_type", Naming.shortCanonicalName(primaryKeyField.javaType()),
            "$pk_annotation", primaryKeyField.javaType().isPrimitive() ? "" : "@Nonnull ",
            "$pk_name", primaryKeyField.javaName(),
            "$pk_sql", ((SimpleTableField) primaryKeyField).column().sqlName()
        );
    }

    public void generateJava() throws IOException {
        imports();

        classDef();
        constructor();

        count();
        // isEmpty();

        getByPk();
        iterator();

        insert();
        valuesForInsert();
        update();
        valuesForUpdate();

        fromRow();

        appendLine("}");
    }

    private void imports() throws IOException {
        List<Class<?>> customClasses = table.fields().stream()
                .filter(TableField::isCustomSupportType)
                .map(TableField::javaType)
                .collect(Collectors.toList());

        List<String> classesToImport = Streams.concat(
            Stream.of(pickBaseTableClass(), QueryRunner.class, QueryException.class, ResultSetIterator.class).map(FQN::of),
            customClasses.stream().map(FQN::of),
            customClasses.stream().map(adaptersLocator::locateAdapterFqn)
        ).filter(fqn -> !isSkippablePackage(fqn.packageName())).map(FQN::importName).sorted().distinct().toList();

        Map<String, String> context = Map.of(
            "$package", table.packageName(),
            "$imports", classesToImport.stream().map("import %s;"::formatted).collect(LINE_JOINER)
        );

        appendCode(0, """
        package $package;

        import java.sql.*;
        import java.util.*;
        import java.util.function.*;
        import javax.annotation.*;
        
        $imports\n
        """, context);
    }

    private boolean isSkippablePackage(@NotNull String packageName) {
        return packageName.equals(table.packageName()) || packageName.equals("java.util") || packageName.equals("java.lang");
    }

    private void classDef() throws IOException {
        Class<?> baseTableClass = pickBaseTableClass();
        Map<String, String> context = Map.of(
            "$BaseClass", Naming.shortCanonicalName(baseTableClass),
            "$BaseGenerics", baseTableClass == TableObj.class ? "$pk_type, $ModelClass" : "$ModelClass"
        );
        appendCode(0, "public class $TableClass implements $BaseClass<$BaseGenerics> {",
                   EasyMaps.merge(context, mainContext, pkContext));
    }

    private @NotNull Class<?> pickBaseTableClass() {
        TableField primaryKeyField = table.primaryKeyField();
        if (primaryKeyField == null) {
            return BaseTable.class;
        }
        Class<?> javaType = primaryKeyField.javaType();
        if (javaType == int.class) {
            return TableInt.class;
        }
        if (javaType == long.class) {
            return TableLong.class;
        }
        return TableObj.class;
    }

    private void constructor() throws IOException {
        appendCode("""
        protected final Connection connection;
        protected final QueryRunner runner;
    
        public $TableClass(@Nonnull Connection connection) {
            this.connection = connection;
            this.runner = new QueryRunner(connection);
        }\n
        """, mainContext);
    }

    private void count() throws IOException {
        appendCode("""
        @Override
        public int count() {
            String query = "SELECT COUNT(*) FROM $table_sql";
            try (ResultSet result = runner.runQuery(query)) {
                return result.getInt(1);
            } catch (SQLException e) {
                throw new QueryException("$table_sql.count() failed", query, e);
            }
        }\n
        """, mainContext);
    }

    private void isEmpty() throws IOException {
        appendCode("""
        public boolean isEmpty() {
            return count() == 0;
        }\n
        """);
    }

    private void getByPk() throws IOException {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        List<Column> primaryColumns = table.columns(TableField::isPrimaryKey);
        Map<String, String> context = EasyMaps.asMap(
            "$pk_cols_assign", joinWithPattern(primaryColumns, EQ_QUESTION)
        );

        appendCode("""
        @Override
        public @Nullable $ModelClass getByPkOrNull($pk_annotation$pk_type $pk_name) {
            String query = "SELECT $all_columns FROM $table_sql WHERE $pk_cols_assign";
            try (ResultSet result = runner.runQuery(query, $pk_name)) {
                return result.next() ? fromRow(result) : null;
            } catch (SQLException e) {
                throw new QueryException("$table_sql.getByPkOrNull() failed", query, $pk_name, e);
            }
        }
        
        @Override
        public @Nonnull $ModelClass getByPkOrDie($pk_annotation$pk_type $pk_name) {
            return Objects.requireNonNull(getByPkOrNull($pk_name), "$table_sql not found by PK: $pk_name=" + $pk_name);
        }
        
        @Override
        public @Nonnull Optional<$ModelClass> getOptionalByPk($pk_annotation$pk_type $pk_name) {
            return Optional.ofNullable(getByPkOrNull($pk_name));
        }\n
        """, EasyMaps.merge(mainContext, pkContext, context));
    }

    private void iterator() throws IOException {
        appendCode("""
        @Override
        public void forEach(@Nonnull Consumer<? super $ModelClass> consumer) {
            String query = "SELECT $all_columns FROM $table_sql";
            try (ResultSet result = runner.runQuery(query)) {
                while (result.next()) {
                    consumer.accept(fromRow(result));
                }
            } catch (SQLException e) {
                throw new QueryException("$table_sql.forEach() failed", query, e);
            }
        }
    
        @Override
        public @Nonnull ResultSetIterator<$ModelClass> iterator() {
            String query = "SELECT $all_columns FROM $table_sql";
            try {
                return new ResultSetIterator<>(runner.runQuery(query), $TableClass::fromRow);
            } catch (SQLException e) {
                throw new QueryException("$table_sql.iterator() failed", query, e);
            }
        }\n
        """, mainContext);
    }

    private void insert() throws IOException {
        Map<String, String> context = Map.of(
            "$placeholders", Stream.generate(() -> "?").limit(table.columnsNumber()).collect(COMMA_JOINER)
        );
        
        appendCode("""
        @Override
        public int insert(@Nonnull $ModelClass $model_param) {
           String query = "INSERT INTO $table_sql($all_columns) VALUES($placeholders)";
           try {
               return runner.runUpdate(query, valuesForInsert($model_param));
           } catch (SQLException e) {
               throw new QueryException("$table_sql.insert() failed", query, $model_param, e);
           }
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private void valuesForInsert() throws IOException {
        List<FieldArrayConversion> conversions = zipWithColumnIndex(table.fields(), FieldArrayConversion::new);
        Map<String, String> context = Map.of(
            "$array_init", conversions.stream().map(FieldArrayConversion::initLine).collect(linesJoiner(INDENT2)),
            "$array_convert", conversions.stream().map(FieldArrayConversion::fillValuesLine).collect(linesJoiner(INDENT, true))
        );

        appendCode("""
        protected static @Nonnull Object[] valuesForInsert(@Nonnull $ModelClass $model_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private void update() throws IOException {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        List<Column> primary = table.columns(TableField::isPrimaryKey);
        List<Column> nonPrimary = table.columns(Predicate.not(TableField::isPrimaryKey));
        Map<String, String> context = EasyMaps.asMap(
            "$pk_cols_assign", joinWithPattern(primary, EQ_QUESTION),
            "$npk_cols_assign", joinWithPattern(nonPrimary, EQ_QUESTION)
        );

        appendCode("""
        @Override
        public int updateByPk(@Nonnull $ModelClass $model_param) {
           String query = "UPDATE $table_sql SET $npk_cols_assign WHERE $pk_cols_assign";
           try {
               return runner.runUpdate(query, valuesForUpdate($model_param));
           } catch (SQLException e) {
               throw new QueryException("$table_sql.insert() failed", query, $model_param, e);
           }
        }\n
        """, EasyMaps.merge(mainContext, pkContext, context));
    }

    private void valuesForUpdate() throws IOException {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        List<TableField> primary = table.fields().stream().filter(TableField::isPrimaryKey).toList();
        List<TableField> nonPrimary = table.fields().stream().filter(Predicate.not(TableField::isPrimaryKey)).toList();
        Iterable<TableField> fields = Iterables.concat(nonPrimary, primary);

        List<FieldArrayConversion> conversions = zipWithColumnIndex(fields, FieldArrayConversion::new);
        Map<String, String> context = Map.of(
            "$array_init", conversions.stream().map(FieldArrayConversion::initLine).collect(linesJoiner(INDENT2)),
            "$array_convert", conversions.stream().map(FieldArrayConversion::fillValuesLine).collect(linesJoiner(INDENT, true))
        );

        appendCode("""
        protected static @Nonnull Object[] valuesForUpdate(@Nonnull $ModelClass $model_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private record FieldArrayConversion(@NotNull TableField field, int columnIndex) {
        public @NotNull String initLine() {
            if (!field.isNativelySupportedType()) {
                return "null,";
            }
            return "$model_param.%s(),".formatted(field.javaGetter().getName());
        }

        public @NotNull String fillValuesLine() {
            if (field.isNativelySupportedType()) {
                return "";
            }
            return "%s.fillArrayValues($model_param.%s(), array, %d);"
                    .formatted(requireNonNull(field.adapterInfo()).staticRef(), field.javaGetter().getName(), columnIndex);
        }
    }

    private static <T> @NotNull List<T> zipWithColumnIndex(@NotNull Iterable<TableField> fields,
                                                           @NotNull ObjIntBiFunction<TableField, T> converter) {
        ArrayList<T> result = new ArrayList<>();
        int columnIndex = 0;
        for (TableField field : fields) {
            result.add(converter.apply(field, columnIndex));
            columnIndex += field.columnsNumber();
        }
        return result;
    }

    private void fromRow() throws IOException {
        Map<String, String> context = Map.of(
            "$fields_assignments", new ResultSetConversionGenerator().generateAssignments(table),
            "$model_fields", table.fields().stream().map(TableField::javaName).collect(COMMA_JOINER)
        );

        appendCode("""
        public static @Nonnull $ModelClass fromRow(@Nonnull ResultSet result) throws SQLException {
        $fields_assignments
            return new $ModelClass($model_fields);
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private static class ResultSetConversionGenerator {
        private static final Set<Class<?>> FULL_NAME_CLASSES = Set.of(java.util.Date.class, java.sql.Date.class);
        private int columnIndex = 0;

        public @NotNull String generateAssignments(@NotNull TableSchema table) {
            return table.fields().stream().map(this::assignFieldLine).collect(linesJoiner(INDENT));
        }

        private @NotNull String assignFieldLine(@NotNull TableField field) {
            Class<?> fieldType = field.javaType();
            String fieldClassName = FULL_NAME_CLASSES.contains(fieldType) ?
                    fieldType.getName() :
                    Naming.shortCanonicalName(fieldType);
            return "%s %s = %s;".formatted(fieldClassName, field.javaName(), createExpr(field));
        }

        private @NotNull String createExpr(@NotNull TableField field) {
            if (field.isNativelySupportedType()) {
                assert field instanceof SimpleTableField;
                return resultSetGetterExpr(((SimpleTableField) field).column(), ++columnIndex);
            } else {
                String staticRef = requireNonNull(field.adapterInfo()).staticRef();
                String params = field instanceof SimpleTableField simpleField ?
                        resultSetGetterExpr(simpleField.column(), ++columnIndex) :
                        joinWithTransform(field.columns(), column -> resultSetGetterExpr(column, ++columnIndex));
                return "%s.createInstance(%s)".formatted(staticRef, params);
            }
        }

        private static @NotNull String resultSetGetterExpr(@NotNull Column column, int columnIndex) {
            String getter = column.type().jdbcType().getterMethod();
            return "result.%s(%d)".formatted(getter, columnIndex);
        }
    }
}
