package io.webby.util.sql.codegen;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import io.webby.util.collect.EasyMaps;
import io.webby.util.func.ObjIntBiFunction;
import io.webby.util.sql.api.*;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.webby.util.sql.codegen.ColumnJoins.*;
import static io.webby.util.sql.codegen.JavaSupport.wrapAsStringLiteral;
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
            "$model_param", table.modelName().toLowerCase()
        );

        TableField primaryKeyField = table.primaryKeyField();
        this.pkContext = primaryKeyField == null ? Map.of() : EasyMaps.asMap(
            "$pk_type", Naming.shortCanonicalName(primaryKeyField.javaType()),
            "$pk_annotation", primaryKeyField.javaType().isPrimitive() ? "" : "@Nonnull ",
            "$pk_name", primaryKeyField.javaName()
        );
    }

    public void generateJava() throws IOException {
        imports();

        classDef();
        constructor();
        withFollowOnRead();

        count();
        // isEmpty();

        selectConstants();

        getByPk();
        iterator();

        insert();
        valuesForInsert();
        update();
        valuesForUpdate();

        fromRow();

        meta();

        appendLine("}");
    }

    private void imports() throws IOException {
        List<Class<?>> customClasses = table.fields().stream()
                .filter(TableField::isCustomSupportType)
                .map(TableField::javaType)
                .collect(Collectors.toList());

        List<String> classesToImport = Streams.concat(
            Stream.of(pickBaseTableClass(),
                      QueryRunner.class, QueryException.class,
                      ResultSetIterator.class, ReadFollow.class, TableMeta.class).map(FQN::of),
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
        protected final ReadFollow follow;
    
        public $TableClass(@Nonnull Connection connection, @Nonnull ReadFollow follow) {
            this.connection = connection;
            this.runner = new QueryRunner(connection);
            this.follow = follow;
        }
        
        public $TableClass(@Nonnull Connection connection) {
            this(connection, ReadFollow.NO_FOLLOW);
        }\n
        """, mainContext);
    }

    private void withFollowOnRead() throws IOException {
        String code = (table.hasForeignKeyField()) ?
            """
            @Override
            public @Nonnull $TableClass withReferenceFollowOnRead(@Nonnull ReadFollow follow) {
                return this.follow == follow ? this : new $TableClass(connection, follow);
            }\n
            """ :
            """
            @Override
            public @Nonnull $TableClass withReferenceFollowOnRead(@Nonnull ReadFollow follow) {
                return this;
            }\n
            """;
        appendCode(code, mainContext);
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

    private void selectConstants() throws IOException {
        String constants = Arrays.stream(ReadFollow.values())
                .map(follow -> new SelectMaker(table).make(follow))
                .map(snippet -> new Snippet().withLines(snippet))
                .map(query -> wrapAsStringLiteral(query, INDENT))
                .collect(Collectors.joining(",\n" + INDENT, INDENT, ""));

        appendCode("""
        private static final String[] SELECT_ENTITY_ALL = {
        $constants
        };\n
        """, EasyMaps.asMap("$constants", constants));
    }

    private void getByPk() throws IOException {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        List<Column> primaryColumns = table.columns(TableField::isPrimaryKey);
        Snippet where = new Snippet().withLines(WhereMaker.makeForAnd(primaryColumns));
        Map<String, String> context = EasyMaps.asMap(
            "$sql_where_literal", wrapAsStringLiteral(where, INDENT2),
            "$pk_object", toKeyObject(requireNonNull(table.primaryKeyField()), "$pk_name")
        );

        appendCode("""
        @Override
        public @Nullable $ModelClass getByPkOrNull($pk_annotation$pk_type $pk_name) {
            String query = SELECT_ENTITY_ALL[follow.ordinal()] + $sql_where_literal;
            try (ResultSet result = runner.runQuery(query, $pk_object)) {
                return result.next() ? fromRow(result) : null;
            } catch (SQLException e) {
                throw new QueryException("$table_sql.getByPkOrNull() failed", query, $pk_name, e);
            }
        }\n
        """, EasyMaps.merge(context, mainContext, pkContext));
    }

    private static @NotNull String toKeyObject(@NotNull TableField field, @NotNull String paramName) {
        if (field.isNativelySupportedType()) {
            return paramName;
        }
        AdapterInfo adapterInfo = requireNonNull(field.adapterInfo());
        if (field.columnsNumber() == 1) {
            return "%s.toValueObject(%s)".formatted(adapterInfo.staticRef(), paramName);
        } else {
            return "%s.toNewValuesArray(%s)".formatted(adapterInfo.staticRef(), paramName);
        }
    }

    private void iterator() throws IOException {
        appendCode("""
        @Override
        public void forEach(@Nonnull Consumer<? super $ModelClass> consumer) {
            String query = SELECT_ENTITY_ALL[follow.ordinal()];
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
            String query = SELECT_ENTITY_ALL[follow.ordinal()];
            try {
                return new ResultSetIterator<>(runner.runQuery(query), $TableClass::fromRow);
            } catch (SQLException e) {
                throw new QueryException("$table_sql.iterator() failed", query, e);
            }
        }\n
        """, mainContext);
    }

    private void insert() throws IOException {
        Snippet query = InsertMaker.makeAll(table);
        Map<String, String> context = Map.of(
            "$sql_query_literal", wrapAsStringLiteral(query, INDENT2)
        );
        
        appendCode("""
        @Override
        public int insert(@Nonnull $ModelClass $model_param) {
           String query = $sql_query_literal;
           try {
               return runner.runUpdate(query, valuesForInsert($model_param));
           } catch (SQLException e) {
               throw new QueryException("Failed to insert entity into $TableClass", query, $model_param, e);
           }
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private void valuesForInsert() throws IOException {
        ValuesArrayMaker maker = new ValuesArrayMaker("$model_param", table.fields());
        Map<String, String> context = Map.of(
            "$array_init", maker.makeInitValues().join(linesJoiner(INDENT2)),
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT, true))
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

        List<Column> primaryColumns = table.columns(TableField::isPrimaryKey);
        List<Column> nonPrimaryColumns = table.columns(Predicate.not(TableField::isPrimaryKey));
        Snippet query = new Snippet()
                .withLines(UpdateMaker.make(table, nonPrimaryColumns))
                .withLines(WhereMaker.makeForAnd(primaryColumns));
        Map<String, String> context = EasyMaps.asMap(
            "$sql_query_literal", wrapAsStringLiteral(query, INDENT2)
        );

        appendCode("""
        @Override
        public int updateByPk(@Nonnull $ModelClass $model_param) {
           String query = $sql_query_literal;
           try {
               return runner.runUpdate(query, valuesForUpdate($model_param));
           } catch (SQLException e) {
               throw new QueryException("Failed to update entity in $TableClass by PK", query, $model_param, e);
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
        ValuesArrayMaker maker = new ValuesArrayMaker("$model_param", Iterables.concat(nonPrimary, primary));
        Map<String, String> context = Map.of(
            "$array_init", maker.makeInitValues().join(linesJoiner(INDENT2)),
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT, true))
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

    private void fromRow() throws IOException {
        Map<String, String> context = Map.of(
            "$fields_assignments", new ResultSetConversionMaker().make(table),
            "$model_fields", table.fields().stream().map(TableField::javaName).collect(COMMA_JOINER)
        );

        appendCode("""
        public static @Nonnull $ModelClass fromRow(@Nonnull ResultSet result) throws SQLException {
          return fromRow(result, 0);
        }
        
        public static @Nonnull $ModelClass fromRow(@Nonnull ResultSet result, int start) throws SQLException {
        $fields_assignments
            return new $ModelClass($model_fields);
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private void meta() throws IOException {
        Map<String, String> context = Map.of(
            "$column_strings", joinWithPattern(table.columns(), "\"%s\"")
        );

        appendCode("""
        public static final TableMeta META = new TableMeta() {
            @Override
            public @Nonnull String sqlTableName() {
                return "$table_sql";
            }
            @Override
            public @Nonnull List<String> sqlColumns() {
                return List.of($column_strings);
            }
        };
        
        @Override
        public @Nonnull TableMeta meta() {
            return META;
        }
        """, EasyMaps.merge(mainContext, context));
    }
}
