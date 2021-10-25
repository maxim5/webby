package io.webby.util.sql;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Primitives;
import io.webby.util.EasyMaps;
import io.webby.util.Pair;
import io.webby.util.sql.api.*;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.webby.util.sql.schema.ColumnJoins.*;

@SuppressWarnings("UnnecessaryStringEscape")
public class DataTableGenerator extends BaseGenerator {
    private final TableSchema table;
    private final Options options;

    private final Map<String, String> mainContext;
    private final Map<String, String> pkContext;

    public DataTableGenerator(@NotNull TableSchema table, @NotNull Appendable writer, @NotNull Options options) {
        super(writer);
        this.table = table;
        this.options = options;

        this.mainContext = EasyMaps.asMap(
            "$sql_table", table.sqlName(),
            "$DataClass", table.dataClass().getSimpleName(),
            "$data_param", table.dataClass().getSimpleName().toLowerCase(),
            "$all_columns", joinWithComma(table.columns())
        );

        TableField primaryKeyField = table.primaryKeyField();
        this.pkContext = primaryKeyField == null ? Map.of() : EasyMaps.asMap(
            "$pk_type", primaryKeyField.javaType().getTypeName(),
            "$pk_name", primaryKeyField.javaName(),
            "$pk_sql", ((SimpleTableField) primaryKeyField).column().sqlName()
        );
    }

    private static boolean requiresDataConverter(@NotNull TableField field) {
        Class<?> javaType = field.javaType();
        if (isPrimitive(javaType)) {
            return false;
        }
        // Check method exists in SqlDataConverter.
        return true;
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
        List<Class<?>> classesToImport = List.of(QueryRunner.class, QueryException.class, TableLong.class);
        List<Pair<String, String>> dataConvertersToImport = table.fields().stream()
                .filter(DataTableGenerator::requiresDataConverter)
                .map(TableField::javaType)
                .map(type -> Pair.of(type.getPackageName(), "%sDataConverter".formatted(type.getSimpleName())))
                .toList();

        Map<String, String> context = Map.of(
            "$package", options.packageName(),
            "$imports", classesToImport.stream().map(Class::getPackageName).distinct()
                                .map("import %s.*;"::formatted)
                                .collect(Collectors.joining("\n")),
            "$static_imports", dataConvertersToImport.stream()
                                .map(fqn -> "import static %s.%s.*;".formatted(fqn.first(), fqn.second()))
                                .collect(Collectors.joining("\n"))
        );

        appendCode(0, """
        package $package;

        import java.sql.*;
        import java.util.*;
        import java.util.function.*;
        import javax.annotation.*;
        
        $imports
        $static_imports\n
        """, context);
    }

    private void classDef() throws IOException {
        String baseClass = "%s<%s>".formatted(TableLong.class.getSimpleName(), table.dataClass().getSimpleName());
        appendCode(0, "public class $ClassTable implements $BaseClass {", Map.of(
            "$ClassTable", table.javaName(),
            "$BaseClass", baseClass
        ));
    }

    private void constructor() throws IOException {
        appendCode("""
        private final Connection connection;
        private final QueryRunner runner;
    
        public $ClassTable(Connection connection) {
            this.connection = connection;
            this.runner = new QueryRunner(connection);
        }\n
        """, Map.of("$ClassTable", table.javaName()));
    }

    private void count() throws IOException {
        appendCode("""
        @Override
        public int count() {
            String query = "SELECT COUNT(*) FROM $sql_table";
            try (ResultSet result = runner.runQuery(query)) {
                return result.getInt(1);
            } catch (SQLException e) {
                throw new QueryException("$sql_table.count() failed", query, e);
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
        public @Nullable $DataClass getByPkOrNull($pk_type $pk_name) {
            String query = "SELECT $all_columns FROM $sql_table WHERE $pk_cols_assign";
            try (ResultSet result = runner.runQuery(query, $pk_name)) {
                return result.next() ? fromRow(result) : null;
            } catch (SQLException e) {
                throw new QueryException("$sql_table.getByPkOrNull() failed", query, $pk_name, e);
            }
        }
        
        @Override
        public @Nonnull $DataClass getByPkOrDie($pk_type $pk_name) {
            return Objects.requireNonNull(getByPkOrNull($pk_name), "$sql_table not found by PK: $pk_name=" + $pk_name);
        }
        
        @Override
        public @Nonnull Optional<$DataClass> getOptionalByPk($pk_type $pk_name) {
            return Optional.ofNullable(getByPkOrNull($pk_name));
        }\n
        """, EasyMaps.merge(mainContext, pkContext, context));
    }

    private void iterator() throws IOException {
        appendCode("""
        @Override
        public void forEach(@Nonnull Consumer<? super $DataClass> consumer) {
            String query = "SELECT $all_columns FROM $sql_table";
            try (ResultSet result = runner.runQuery(query)) {
                while (result.next()) {
                    consumer.accept(fromRow(result));
                }
            } catch (SQLException e) {
                throw new QueryException("$sql_table.forEach() failed", query, e);
            }
        }
    
        @Override
        public @Nonnull ResultSetIterator<$DataClass> iterator() {
            String query = "SELECT $all_columns FROM $sql_table";
            try {
                return new ResultSetIterator<>(runner.runQuery(query), UserTable::fromRow);
            } catch (SQLException e) {
                throw new QueryException("$sql_table.iterator() failed", query, e);
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
        public int insert(@Nonnull $DataClass $data_param) {
           String query = "INSERT INTO $sql_table($all_columns) VALUES($placeholders)";
           try {
               return runner.runUpdate(query, valuesForInsert($data_param));
           } catch (SQLException e) {
               throw new QueryException("$sql_table.insert() failed", query, $data_param, e);
           }
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private void valuesForInsert() throws IOException {
        ArrayConversionGenerator generator = new ArrayConversionGenerator();
        generator.generateConvert(table.fields());
        Map<String, String> context = Map.of(
            "$array_init", generator.getArrayInit(),
            "$array_convert", generator.getArrayConvert()
        );

        appendCode("""
        private static @Nonnull Object[] valuesForInsert(@Nonnull $DataClass $data_param) {
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
        public int updateByPk(@Nonnull $DataClass $data_param) {
           String query = "UPDATE $sql_table SET $npk_cols_assign WHERE $pk_cols_assign";
           try {
               return runner.runUpdate(query, valuesForUpdate($data_param));
           } catch (SQLException e) {
               throw new QueryException("$sql_table.insert() failed", query, $data_param, e);
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

        ArrayConversionGenerator generator = new ArrayConversionGenerator();
        generator.generateConvert(fields);
        Map<String, String> context = Map.of(
            "$array_init", generator.getArrayInit(),
            "$array_convert", generator.getArrayConvert()
        );

        appendCode("""
        private static @Nonnull Object[] valuesForUpdate(@Nonnull $DataClass $data_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private static class ArrayConversionGenerator {
        private final List<TableField> initPerColumns = new ArrayList<>();
        private final List<Pair<TableField, Integer>> convertAccess = new ArrayList<>();

        public void generateConvert(@NotNull Iterable<TableField> fields) {
            int columnIndex = 0;
            for (TableField field : fields) {
                int columnsNumber = field.columnsNumber();
                if (!requiresDataConverter(field)) {
                    initPerColumns.add(field);
                } else {
                    for (int i = 0; i < columnsNumber; i++) {
                        initPerColumns.add(null);
                    }
                    convertAccess.add(Pair.of(field, columnIndex));
                }
                columnIndex += columnsNumber;
            }
        }

        public @NotNull String getArrayInit() {
            return initPerColumns.stream().map(field -> {
                if (field == null) {
                    return "null,";
                }
                return "$data_param.%s(),".formatted(field.javaGetter().getName());
            }).map(s -> "        " + s).collect(Collectors.joining("\n"));
        }

        public @NotNull String getArrayConvert() {
            return convertAccess.stream().map(pair -> {
                return "toArray($data_param.%s(), array, %d);".formatted(pair.first().javaGetter().getName(), pair.second());
            }).map(s -> "    " + s).collect(Collectors.joining("\n"));
        }
    }

    private void fromRow() throws IOException {
        Map<String, String> context = Map.of(
            "$fields_assignments", new ResultSetConversionGenerator().generateAssignments(table),
            "$data_fields", table.fields().stream().map(TableField::javaName).collect(COMMA_JOINER)
        );

        appendCode("""
        private static @Nonnull $DataClass fromRow(ResultSet result) throws SQLException {
        $fields_assignments
            return new $DataClass($data_fields);
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private static class ResultSetConversionGenerator {
        private int columnIndex = 0;

        public @NotNull String generateAssignments(@NotNull TableSchema table) {
            StringBuilder builder = new StringBuilder();
            for (TableField field : table.fields()) {
                String fieldName = field.javaName();
                Class<?> fieldType = field.javaType();

                String lhs = "%s %s".formatted(fieldType.getSimpleName(), fieldName);
                String rhs;

                if (isPrimitive(fieldType)) {
                    assert field instanceof SimpleTableField;
                    rhs = resultSetGetterExpr(((SimpleTableField) field).column(), ++columnIndex);
                } else if (field instanceof SimpleTableField simpleField) {
                    String param = resultSetGetterExpr(simpleField.column(), ++columnIndex);
                    rhs = "convertTo%s(%s)".formatted(fieldType.getSimpleName(), param);
                } else {
                    String params = joinWithTransform(field.columns(), column -> resultSetGetterExpr(column, ++columnIndex));
                    rhs = "convertTo%s(%s)".formatted(fieldType.getSimpleName(), params);
                }

                builder.append("    %s = %s;\n".formatted(lhs, rhs));
            }
            return builder.toString();
        }

        private static final Map<SqlDataFamily, String> RESULT_SET_GETTERS = EasyMaps.asMap(
            SqlDataFamily.Boolean, "getBoolean",
            SqlDataFamily.Integer, "getInt",
            SqlDataFamily.Long, "getLong",
            SqlDataFamily.Float, "getFloat",
            SqlDataFamily.Double, "getDouble",
            SqlDataFamily.FixedString, "getString",
            SqlDataFamily.VariableString, "getString",
            SqlDataFamily.Binary, "getBytes"
        );

        private static @NotNull String resultSetGetterExpr(@NotNull Column column, int columnIndex) {
            String getter = RESULT_SET_GETTERS.get(column.type().family());
            return "result.%s(%d)".formatted(getter, columnIndex);
        }
    }

    public record Options(@NotNull String packageName) {
        public static @NotNull Options of(@NotNull Class<?> dataClass) {
            return new Options(dataClass.getPackageName());
        }

        public @NotNull String directoryName() {
            return packageName.replaceAll("\\.", "/");
        }
    }

    private static boolean isPrimitive(@NotNull Class<?> javaType) {
        return javaType.isPrimitive() || Primitives.isWrapperType(javaType);
    }
}
