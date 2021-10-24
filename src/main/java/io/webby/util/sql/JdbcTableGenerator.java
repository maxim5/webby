package io.webby.util.sql;

import io.webby.util.EasyMaps;
import io.webby.util.sql.api.QueryRunner;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.webby.util.sql.schema.ColumnJoins.*;

@SuppressWarnings("UnnecessaryStringEscape")
public class JdbcTableGenerator {
    private final TableSchema table;
    private final Appendable writer;
    private final Options options;

    private final Map<String, String> mainContext;
    private final Map<String, String> pkContext;

    public JdbcTableGenerator(@NotNull TableSchema table, @NotNull Appendable writer, @NotNull Options options) {
        this.table = table;
        this.writer = writer;
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

    public void generateJava() throws IOException {
        imports();

        classDef();
        constructor();

        count();
        // isEmpty();

        getByPk();
        iterator();

        insert();
        update();

        fromRow();

        appendLine("}");
    }

    private void imports() throws IOException {
        Map<String, String> context = Map.of(
            "$package", options.packageName(),
            "$sql_utils", QueryRunner.class.getPackageName()
        );

        appendCode(0, """
        package $package;

        import $sql_utils.*;
        
        import java.sql.*;
        import java.util.*;
        import java.util.function.*;
        import javax.annotation.*;\n
        """, context);
    }

    private void classDef() throws IOException {
        String baseClass = "LongKeyTable<%s>".formatted(table.dataClass().getSimpleName());
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
                return fromRow(result);
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
        }
        
        private static @Nonnull Object[] valuesForInsert(@Nonnull $DataClass $data_param) {
            return new Object[] {
                $data_param.userId(),
                $data_param.access().level(),
            };
        }\n
        """, EasyMaps.merge(mainContext, context));
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
        }
        
        private static @Nonnull Object[] valuesForUpdate(@Nonnull $DataClass $data_param) {
            return new Object[] {
                $data_param.access().level(),
                $data_param.userId(),
            };
        }\n
        """, EasyMaps.merge(mainContext, pkContext, context));
    }

    private void fromRow() throws IOException {
        appendCode("""
        private static @Nullable $DataClass fromRow(ResultSet result) throws SQLException {
            return null;
        }\n
        """, mainContext);
    }

    private JdbcTableGenerator append(@NotNull String value) throws IOException {
        writer.append(value);
        return this;
    }

    private JdbcTableGenerator indent(int level) throws IOException {
        for (int i = 0; i < level; i++) {
            writer.append("    ");
        }
        return this;
    }

    private JdbcTableGenerator appendCode(@NotNull String code) throws IOException {
        return appendCode(1, code, Map.of());
    }

    private JdbcTableGenerator appendCode(@NotNull String code, @NotNull Map<String, String> context) throws IOException {
        return appendCode(1, code, context);
    }

    private JdbcTableGenerator appendCode(int indent, @NotNull String code, @NotNull Map<String, String> context) throws IOException {
        for (Map.Entry<String, String> entry : context.entrySet()) {
            code = code.replace(entry.getKey(), entry.getValue());
        }
        String[] lines = code.lines().toArray(String[]::new);
        for (String line : lines) {
            indent(indent).appendLine(line);
        }
        return this;
    }

    private JdbcTableGenerator appendLine(@NotNull String ... values) throws IOException {
        for (String value : values) {
            writer.append(value);
        }
        writer.append("\n");
        return this;
    }

    public record Options(@NotNull String packageName) {
        public static @NotNull Options of(@NotNull Class<?> dataClass) {
            return new Options(dataClass.getPackageName());
        }

        public @NotNull String directoryName() {
            return packageName.replaceAll("\\.", "/");
        }
    }
}
