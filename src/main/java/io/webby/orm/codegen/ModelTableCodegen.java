package io.webby.orm.codegen;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import io.webby.orm.api.*;
import io.webby.orm.api.query.Filter;
import io.webby.orm.api.query.TermType;
import io.webby.orm.api.query.Where;
import io.webby.orm.arch.*;
import io.webby.util.collect.EasyMaps;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.webby.orm.codegen.JavaSupport.*;
import static io.webby.orm.codegen.Joining.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnnecessaryStringEscape")
public class ModelTableCodegen extends BaseCodegen {
    private final ModelAdaptersScanner adaptersScanner;
    private final TableArch table;

    private final Map<String, String> mainContext;
    private final Map<String, String> pkContext;

    public ModelTableCodegen(@NotNull ModelAdaptersScanner adaptersScanner,
                             @NotNull TableArch table, @NotNull Appendable writer) {
        super(writer);
        this.adaptersScanner = adaptersScanner;
        this.table = table;

        this.mainContext = EasyMaps.asMap(
            "$TableClass", table.javaName(),
            "$table_sql", table.sqlName(),
            "$ModelClass", Naming.shortCanonicalJavaName(table.modelClass()),
            "$model_param", table.modelName().toLowerCase()
        );

        TableField primaryKeyField = table.primaryKeyField();
        this.pkContext = primaryKeyField == null ? Map.of() : EasyMaps.asMap(
            "$pk_type", Naming.shortCanonicalJavaName(primaryKeyField.javaType()),
            "$pk_annotation", primaryKeyField.javaType().isPrimitive() ? "" : "@Nonnull ",
            "$pk_name", primaryKeyField.javaName()
        );
    }

    public void generateJava() throws IOException {
        imports();

        classDef();
        constructors();
        withFollowOnRead();

        getters();
        count();
        exists();

        selectConstants();

        getByPk();
        keyOf();
        iterator();

        insert();
        valuesForInsert();
        insertAutoIncPk();
        valuesForInsertAutoIncPk();

        updateByPk();
        valuesForUpdate();
        deleteByPk();

        fromRow();

        internalMeta();
        columnsEnum();
        tableMeta();

        appendLine("}");
    }

    private void imports() throws IOException {
        List<Class<?>> customClasses = table.fields().stream()
                .filter(TableField::isCustomSupportType)
                .filter(Predicate.not(TableField::isForeignKey))
                .map(TableField::javaType)
                .collect(Collectors.toList());

        List<Class<?>> foreignKeyClasses = table.hasForeignKeyField() ?
                List.of(Foreign.class, ForeignInt.class, ForeignLong.class, ForeignObj.class) :
                List.of();
        /*List<Class<?>> foreignKeyClasses = table.foreignFields(ReadFollow.FOLLOW_ALL).stream()
                .map(TableField::javaType)
                .collect(Collectors.toList());*/
        List<JavaNameHolder> foreignModelClasses = table.foreignFields(ReadFollow.FOLLOW_ALL).stream()
                .map(ForeignTableField::getForeignTable)
                .collect(Collectors.toList());

        List<String> classesToImport = Streams.concat(
            Stream.of(pickBaseTableClass(),
                      Connector.class, QueryRunner.class, QueryException.class, Engine.class, ReadFollow.class,
                      Filter.class, Where.class, io.webby.orm.api.query.Column.class, TermType.class,
                      ResultSetIterator.class, TableMeta.class).map(FQN::of),
            customClasses.stream().map(FQN::of),
            customClasses.stream().map(adaptersScanner::locateAdapterFqn),
            foreignKeyClasses.stream().map(FQN::of),
            foreignModelClasses.stream().map(FQN::of)
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
            "$BaseClass", Naming.shortCanonicalJavaName(baseTableClass),
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

    private void constructors() throws IOException {
        appendCode("""
        protected final Connector connector;
        protected final ReadFollow follow;
    
        public $TableClass(@Nonnull Connector connector, @Nonnull ReadFollow follow) {
            this.connector = connector;
            this.follow = follow;
        }
        
        public $TableClass(@Nonnull Connector connector) {
            this(connector, ReadFollow.NO_FOLLOW);
        }\n
        """, mainContext);
    }

    private void withFollowOnRead() throws IOException {
        String code = (table.hasForeignKeyField()) ?
            """
            @Override
            public @Nonnull $TableClass withReferenceFollowOnRead(@Nonnull ReadFollow follow) {
                return this.follow == follow ? this : new $TableClass(connector, follow);
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

    private void getters() throws IOException {
        appendCode("""
        @Override
        public @Nonnull Engine engine() {
            return connector.engine();
        }
        
        @Override
        public @Nonnull QueryRunner runner() {
            return connector.runner();
        }\n
        """);
    }

    private void count() throws IOException {
        appendCode("""
        @Override
        public int count() {
            String query = "SELECT COUNT(*) FROM $table_sql";
            try (PreparedStatement statement = runner().prepareQuery(query);
                 ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            } catch (SQLException e) {
                throw new QueryException("Failed to count in $TableClass", query, e);
            }
        }
        
        @Override
        public int count(@Nonnull Filter filter) {
            String query = "SELECT COUNT(*) FROM $table_sql\\n" + filter.repr();
            try (PreparedStatement statement = runner().prepareQuery(query, filter.args());
                 ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            } catch (SQLException e) {
                throw new QueryException("Failed to count in $TableClass", query, e);
            }
        }\n
        """, mainContext);
    }

    private void exists() throws IOException {
        appendCode("""
        @Override
        public boolean isNotEmpty() {
            String query = "SELECT EXISTS (SELECT * FROM $table_sql LIMIT 1)";
            try (PreparedStatement statement = runner().prepareQuery(query);
                 ResultSet result = statement.executeQuery()) {
                return result.next() && result.getBoolean(1);
            } catch (SQLException e) {
                throw new QueryException("Failed to check exists in $TableClass", query, e);
            }
        }
    
        @Override
        public boolean exists(@Nonnull Where where) {
            String query = "SELECT EXISTS (SELECT * FROM $table_sql " + where.repr() + " LIMIT 1)";
            try (PreparedStatement statement = runner().prepareQuery(query, where.args());
                 ResultSet result = statement.executeQuery()) {
                return result.next() && result.getBoolean(1);
            } catch (SQLException e) {
                throw new QueryException("Failed to check exists in $TableClass", query, where.args(), e);
            }
        }\n
        """, mainContext);
    }

    private void selectConstants() throws IOException {
        String constants = Arrays.stream(ReadFollow.values())
                .map(follow -> new SelectMaker(table).make(follow))
                .map(snippet -> new Snippet().withLines(snippet))
                .map(query -> wrapAsStringLiteral(query, INDENT1))
                .collect(Collectors.joining(",\n" + INDENT1, INDENT1, ""));

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

        Snippet where = new Snippet().withLines(WhereMaker.makeForPrimaryColumns(table));
        Map<String, String> context = EasyMaps.asMap(
            "$sql_where_literal", wrapAsStringLiteral(where, INDENT2),
            "$pk_object", toKeyObject(requireNonNull(table.primaryKeyField()), "$pk_name")
        );

        appendCode("""
        @Override
        public @Nullable $ModelClass getByPkOrNull($pk_annotation$pk_type $pk_name) {
            String query = SELECT_ENTITY_ALL[follow.ordinal()] + $sql_where_literal;
            try (PreparedStatement statement = runner().prepareQuery(query, $pk_object);
                 ResultSet result = statement.executeQuery()) {
                return result.next() ? fromRow(result, follow, 0) : null;
            } catch (SQLException e) {
                throw new QueryException("Failed to find by PK in $TableClass", query, $pk_name, e);
            }
        }\n
        """, EasyMaps.merge(context, mainContext, pkContext));
    }

    private static @NotNull String toKeyObject(@NotNull TableField field, @NotNull String paramName) {
        if (field.isNativelySupportedType()) {
            return paramName;
        }
        AdapterApi adapterApi = requireNonNull(field.adapterApi());
        if (field.columnsNumber() == 1) {
            return "%s.toValueObject(%s)".formatted(adapterApi.staticRef(), paramName);
        } else {
            return "%s.toNewValuesArray(%s)".formatted(adapterApi.staticRef(), paramName);
        }
    }

    private void keyOf() throws IOException {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        Map<String, String> context = EasyMaps.asMap(
            "$KeyOfMethod", table.isPrimaryKeyInt() ? "intKeyOf" : table.isPrimaryKeyLong() ? "longKeyOf" : "keyOf",
            "$pk_getter", requireNonNull(table.primaryKeyField()).javaGetter()
        );

        appendCode("""
        @Override
        public $pk_annotation$pk_type $KeyOfMethod(@Nonnull $ModelClass $model_param) {
            return $model_param.$pk_getter();
        }\n
        """, EasyMaps.merge(context, mainContext, pkContext));
    }

    private void iterator() throws IOException {
        appendCode("""
        @Override
        public void forEach(@Nonnull Consumer<? super $ModelClass> consumer) {
            String query = SELECT_ENTITY_ALL[follow.ordinal()];
            try (PreparedStatement statement = runner().prepareQuery(query);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    consumer.accept(fromRow(result, follow, 0));
                }
            } catch (SQLException e) {
                throw new QueryException("Failed to iterate over $TableClass", query, e);
            }
        }
    
        @Override
        public @Nonnull ResultSetIterator<$ModelClass> iterator() {
            String query = SELECT_ENTITY_ALL[follow.ordinal()];
            try {
                return ResultSetIterator.of(runner().prepareQuery(query).executeQuery(),
                                            result -> fromRow(result, follow, 0));
            } catch (SQLException e) {
                throw new QueryException("Failed to iterate over $TableClass", query, e);
            }
        }

        @Override
        public @Nonnull ResultSetIterator<$ModelClass> iterator(@Nonnull Filter filter) {
            String query = SELECT_ENTITY_ALL[follow.ordinal()] + filter.repr();
            try {
                return ResultSetIterator.of(runner().prepareQuery(query, filter.args()).executeQuery(),
                                            result -> fromRow(result, follow, 0));
            } catch (SQLException e) {
                throw new QueryException("Failed to iterate over $TableClass", query, filter.args(), e);
            }
        }\n
        """, mainContext);
    }

    private void insert() throws IOException {
        Snippet query = InsertMaker.makeAll(table);
        Map<String, String> context = Map.of(
            "$model_id_assert", AssertModelIdMaker.makeAssert("$model_param", table).join(),
            "$sql_query_literal", wrapAsStringLiteral(query, INDENT2)
        );
        
        appendCode("""
        @Override
        public int insert(@Nonnull $ModelClass $model_param) {
            $model_id_assert
            String query = $sql_query_literal;
            try {
                return runner().runUpdate(query, valuesForInsert($model_param));
            } catch (SQLException e) {
                throw new QueryException("Failed to insert entity into $TableClass", query, $model_param, e);
            }
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private void valuesForInsert() throws IOException {
        ValuesArrayMaker maker = new ValuesArrayMaker("$model_param", table.fields());
        Map<String, String> context = Map.of(
            "$array_init", maker.makeInitValues().join(linesJoiner(INDENT2)),
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT1, true))
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

    private void insertAutoIncPk() throws IOException {
        if (!table.isPrimaryKeyInt() && !table.isPrimaryKeyLong()) {
            return;
        }

        Snippet query = InsertMaker.make(table, table.columns(Predicate.not(TableField::isPrimaryKey)));
        Map<String, String> context = Map.of(
            "$sql_query_literal", wrapAsStringLiteral(query, INDENT2)
        );

        appendCode("""
        @Override
        public $pk_type insertAutoIncPk(@Nonnull $ModelClass $model_param) {
           String query = $sql_query_literal;
           try {
               return ($pk_type) runner().runAutoIncUpdate(query, valuesForInsertAutoIncPk($model_param)).lastId();
           } catch (SQLException e) {
               throw new QueryException("Failed to insert new entity into $TableClass", query, $model_param, e);
           }
        }\n
        """, EasyMaps.merge(mainContext, pkContext, context));
    }

    private void valuesForInsertAutoIncPk() throws IOException {
        if (!table.isPrimaryKeyInt() && !table.isPrimaryKeyLong()) {
            return;
        }

        List<TableField> nonPrimary = table.fields().stream().filter(Predicate.not(TableField::isPrimaryKey)).toList();
        ValuesArrayMaker maker = new ValuesArrayMaker("$model_param", nonPrimary);
        Map<String, String> context = Map.of(
            "$array_init", maker.makeInitValues().join(linesJoiner(INDENT2)),
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT1, true))
        );

        appendCode("""
        protected static @Nonnull Object[] valuesForInsertAutoIncPk(@Nonnull $ModelClass $model_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private void updateByPk() throws IOException {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        Snippet query = new Snippet()
                .withLines(UpdateMaker.make(table, table.columns(Predicate.not(TableField::isPrimaryKey))))
                .withLines(WhereMaker.makeForPrimaryColumns(table));
        Map<String, String> context = EasyMaps.asMap(
            "$sql_query_literal", wrapAsStringLiteral(query, INDENT2)
        );

        appendCode("""
        @Override
        public int updateByPk(@Nonnull $ModelClass $model_param) {
           String query = $sql_query_literal;
           try {
               return runner().runUpdate(query, valuesForUpdate($model_param));
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
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT1, true))
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

    private void deleteByPk() throws IOException {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        Snippet query = new Snippet()
                .withLines(DeleteMaker.make(table))
                .withLines(WhereMaker.makeForPrimaryColumns(table));
        Map<String, String> context = EasyMaps.asMap(
            "$sql_query_literal", wrapAsStringLiteral(query, INDENT2),
            "$pk_object", toKeyObject(requireNonNull(table.primaryKeyField()), "$pk_name")
        );

        appendCode("""
        @Override
        public int deleteByPk($pk_annotation$pk_type $pk_name) {
            String query = $sql_query_literal;
            try {
               return runner().runUpdate(query, $pk_object);
           } catch (SQLException e) {
               throw new QueryException("Failed to delete entity in $TableClass by PK", query, $pk_name, e);
           }
        }\n
        """, EasyMaps.merge(context, mainContext, pkContext));
    }

    private void fromRow() throws IOException {
        ResultSetConversionMaker maker = new ResultSetConversionMaker("result", "follow", "start");
        Map<String, String> context = Map.of(
            "$fields_assignments", maker.make(table).join(linesJoiner(INDENT1)),
            "$model_fields", table.fields().stream().map(TableField::javaName).collect(COMMA_JOINER)
        );

        appendCode("""
        public static @Nonnull $ModelClass fromRow(@Nonnull ResultSet result, @Nonnull ReadFollow follow, int start) throws SQLException {
        $fields_assignments
            return new $ModelClass($model_fields);
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private void internalMeta() throws IOException {
        Map<String, String> context = Map.of(
            "$KeyClass", table.hasPrimaryKeyField() ? "$pk_type.class" : "null"
        );

        appendCode("""
        public static final String NAME = "$table_sql";
        public static final Class<?> KEY_CLASS = $KeyClass;
        public static final Class<?> ENTITY_CLASS = $ModelClass.class;
        public static final Function<Connector, $TableClass> INSTANTIATE = $TableClass::new;\n
        """, EasyMaps.merge(mainContext, context, pkContext));
    }

    private void columnsEnum() throws IOException {
        Map<String, String> context = Map.of(
            "$own_enum_values", ColumnEnumMaker.make(table.columns(ReadFollow.NO_FOLLOW)).join(Collectors.joining(",\n" + INDENT1))
        );

        appendCode("""
        public enum OwnColumn implements Column {
            $own_enum_values;
            
            private final TermType type;
            OwnColumn(TermType type) {
                this.type = type;
            }
            @Override
            public @Nonnull TermType type() {
                return type;
            }
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private void tableMeta() throws IOException {
        Map<String, String> context = Map.of(
            "$column_meta", table.columnsWithFields().stream().map(pair -> {
                TableField field = pair.first();
                boolean primaryKey = field.isPrimaryKey() && field.columnsNumber() == 1;  // SQL doesn't allow multi-PK
                boolean foreignKey = field.isForeignKey();
                Column column = pair.second();
                String sqlName = column.sqlName();
                Class<?> nativeType = column.type().jdbcType().nativeType();
                String type = nativeType.isPrimitive() || nativeType == String.class ?
                                nativeType.getSimpleName() :
                                nativeType == byte[].class ?
                                        "byte[]" :
                                        nativeType.getName();
                return "new ColumnMeta(\"%s\", %s.class, %s, %s)".formatted(sqlName, type, primaryKey, foreignKey);
            }).collect(Collectors.joining(",\n" + INDENT3))
        );

        appendCode("""
        public static final TableMeta META = new TableMeta() {
            @Override
            public @Nonnull String sqlTableName() {
                return "$table_sql";
            }
            @Override
            public @Nonnull List<ColumnMeta> sqlColumns() {
                return List.of(
                    $column_meta
                );
            }
        };
        
        @Override
        public @Nonnull TableMeta meta() {
            return META;
        }
        """, EasyMaps.merge(mainContext, context));
    }
}
