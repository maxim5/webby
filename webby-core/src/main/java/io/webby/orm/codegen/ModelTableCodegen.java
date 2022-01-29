package io.webby.orm.codegen;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.google.common.primitives.Primitives;
import com.google.mu.util.Optionals;
import io.webby.orm.api.*;
import io.webby.orm.api.entity.BatchEntityData;
import io.webby.orm.api.entity.EntityData;
import io.webby.orm.api.query.*;
import io.webby.orm.arch.*;
import io.webby.orm.arch.Column;
import io.webby.util.collect.EasyMaps;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            "$model_param", Naming.variableJavaName(table.modelClass())
        );

        TableField primaryKeyField = table.primaryKeyField();
        this.pkContext = primaryKeyField == null ? Map.of() : EasyMaps.asMap(
            "$pk_type", Naming.shortCanonicalJavaName(primaryKeyField.javaType()),
            "$PkClass", primaryKeyField.javaType().isPrimitive() ?
                    Primitives.wrap(primaryKeyField.javaType()).getSimpleName() :
                    Naming.shortCanonicalJavaName(primaryKeyField.javaType()),
            "$pk_annotation", primaryKeyField.javaType().isPrimitive() ? "" : "@Nonnull ",
            "$pk_name", primaryKeyField.javaName()
        );
    }

    public void generateJava() {
        imports();

        classDef();
        constructors();
        withFollowOnRead();

        getters();
        count();
        exists();

        selectConstants();

        existsByPk();
        getByPk();
        getBatchByPk();
        fetchPks();
        keyOf();
        iterator();

        insert();
        insertIgnore();
        valuesForInsert();
        insertAutoIncPk();
        valuesForInsertAutoIncPk();

        updateByPk();
        valuesForUpdateByPk();
        updateWhere();
        valuesForUpdateWhere();

        insertData();
        updateDataWhere();

        insertBatch();
        updateWhereBatch();

        insertDataBatch();
        updateDataWhereBatch();

        deleteByPk();
        deleteWhere();

        fromRow();

        m2m();

        internalMeta();
        columnsEnum();
        tableMeta();

        appendLine("}");
    }

    private void imports() {
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
        List<JavaNameHolder> foreignTableClasses = table.foreignFields(ReadFollow.FOLLOW_ALL).stream()
                .map(ForeignTableField::getForeignTable)
                .collect(Collectors.toList());
        List<Class<?>> foreignModelClasses = table.isM2M() ?
                Stream.of(table.m2mLeftFieldOrDie(), table.m2mRightFieldOrDie())
                        .map(ForeignTableField::getForeignTable)
                        .map(TableArch::modelClass)
                        .collect(Collectors.toList()) :
                List.of();

        List<String> classesToImport = Streams.concat(
            baseTableClasses().stream().map(FQN::of),
            Stream.of(
                Connector.class, QueryRunner.class, QueryException.class, Engine.class, ReadFollow.class,
                Filter.class, Where.class, Args.class, io.webby.orm.api.query.Column.class, TermType.class,
                ResultSetIterator.class, TableMeta.class, EntityData.class, BatchEntityData.class, Contextual.class
            ).map(FQN::of),
            customClasses.stream().map(FQN::of),
            customClasses.stream().map(adaptersScanner::locateAdapterFqn),
            foreignKeyClasses.stream().map(FQN::of),
            foreignTableClasses.stream().map(FQN::of),
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
        import java.util.stream.*;
        import javax.annotation.*;

        import com.carrotsearch.hppc.*;
        
        $imports\n
        """, context);
    }

    private boolean isSkippablePackage(@NotNull String packageName) {
        return packageName.equals(table.packageName()) || packageName.equals("java.util") || packageName.equals("java.lang");
    }

    private void classDef() {
        Class<?> baseTableClass = pickBaseTableClass();
        Map<String, String> context = Map.of(
            "$BaseClass", Naming.shortCanonicalJavaName(baseTableClass),
            "$BaseGenerics", baseTableClass == TableObj.class ? "$pk_type, $ModelClass" : "$ModelClass"
        );

        if (table.isM2M()) {
            appendCode(0, """
            public class $TableClass implements $BaseClass<$BaseGenerics>, \
            ManyToManyTable<$left_index_wrap, $left_entity, $right_index_wrap, $right_entity> {
            """, EasyMaps.merge(EasyMaps.merge(context, m2mContext()), mainContext, pkContext));
        } else {
            appendCode(0, "public class $TableClass implements $BaseClass<$BaseGenerics> {",
                       EasyMaps.merge(context, mainContext, pkContext));
        }
    }

    private @NotNull List<Class<?>> baseTableClasses() {
        if (table.isM2M()) {
            return List.of(pickBaseTableClass(), ManyToManyTable.class);
        }
        return List.of(pickBaseTableClass());
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

    private void constructors() {
        Optional<String> leftTable = Optionals.optionally(table.isM2M(), () -> table.m2mLeftFieldOrDie().getForeignTable().javaName());
        Optional<String> rightTable = Optionals.optionally(table.isM2M(), () -> table.m2mRightFieldOrDie().getForeignTable().javaName());
        Map<String, String> context = EasyMaps.asMap(
            "$left_table_decl", leftTable.map("protected final %s leftsTable;"::formatted).orElse(EMPTY_LINE),
            "$right_table_decl", rightTable.map("protected final %s rightsTable;"::formatted).orElse(EMPTY_LINE),
            "$left_table_init", leftTable.map("this.leftsTable = new %s(connector, follow);"::formatted).orElse(EMPTY_LINE),
            "$right_table_init", rightTable.map("this.rightsTable = new %s(connector, follow);"::formatted).orElse(EMPTY_LINE)
        );

        appendCode("""
        protected final Connector connector;
        protected final ReadFollow follow;
        $left_table_decl
        $right_table_decl
    
        public $TableClass(@Nonnull Connector connector, @Nonnull ReadFollow follow) {
            this.connector = connector;
            this.follow = follow;
            $left_table_init
            $right_table_init
        }
        
        public $TableClass(@Nonnull Connector connector) {
            this(connector, ReadFollow.NO_FOLLOW);
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private void withFollowOnRead() {
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

    private void getters() {
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

    private void count() {
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

    private void exists() {
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

    private void selectConstants() {
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

    private void existsByPk() {
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
        public boolean exists($pk_annotation$pk_type $pk_name) {
            String query = "SELECT EXISTS (SELECT * FROM $table_sql " + $sql_where_literal + " LIMIT 1)";
            try (PreparedStatement statement = runner().prepareQuery(query, $pk_object);
                 ResultSet result = statement.executeQuery()) {
                return result.next() && result.getBoolean(1);
            } catch (SQLException e) {
                throw new QueryException("Failed to check exists in $TableClass", query, $pk_name, e);
            }
        }\n
        """, EasyMaps.merge(context, mainContext, pkContext));
    }

    private void getByPk() {
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

    private void getBatchByPk() {
        TableField primaryField = table.primaryKeyField();
        if (primaryField == null || !primaryField.isNativelySupportedType()) {
            return;  // non-native fields require keys conversion
        }

        List<String> primaryColumns = primaryField.columns(ReadFollow.NO_FOLLOW)
                .stream()
                .map(PrefixedColumn::sqlPrefixedName)
                .toList();
        if (primaryColumns.size() != 1) {
            return;  // will use a slow default implementation
        }

        String queryExecution = """
            String query = SELECT_ENTITY_ALL[follow.ordinal()] + "WHERE $pk_column IN (" + "?,".repeat(keys.size() - 1) + "?)";
            try (PreparedStatement statement = runner().prepareQuery(query, keys);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    $ModelClass entity = fromRow(result, follow, 0);
                    map.put(entity.$pk_getter(), entity);
                }
            } catch (SQLException e) {
                throw new QueryException("Failed to find by PK batch in $TableClass", query, keys, e);
            }\
        """;

        Map<String, String> context = EasyMaps.asMap(
            "$query_execution", queryExecution,
            "$pk_column", primaryColumns.get(0),
            "$pk_getter", primaryField.javaGetter()
        );

        appendCode("""
        @Override
        public @Nonnull Map<$PkClass, $ModelClass> getBatchByPk(@Nonnull Collection<? extends $PkClass> keys) {
            if (keys.isEmpty()) {
                return Map.of();
            }
            HashMap<$PkClass, $ModelClass> map = new HashMap<>(keys.size());
        $query_execution
            return map;
        }\n
        """, EasyMaps.merge(context, mainContext, pkContext));

        if (table.isPrimaryKeyInt()) {
            appendCode("""
            @Override
            public @Nonnull IntObjectMap<$ModelClass> getBatchByPk(@Nonnull IntContainer keys) {
                if (keys.isEmpty()) {
                    return new IntObjectHashMap<>();
                }
                IntObjectHashMap<$ModelClass> map = new IntObjectHashMap<>(keys.size());
            $query_execution
                return map;
            }\n
            """, EasyMaps.merge(context, mainContext, pkContext));
        }
        if (table.isPrimaryKeyLong()) {
            appendCode("""
            @Override
            public @Nonnull LongObjectMap<$ModelClass> getBatchByPk(@Nonnull LongContainer keys) {
                if (keys.isEmpty()) {
                    return new LongObjectHashMap<>();
                }
                LongObjectHashMap<$ModelClass> map = new LongObjectHashMap<>(keys.size());
            $query_execution
                return map;
            }\n
            """, EasyMaps.merge(context, mainContext, pkContext));
        }
    }

    private void fetchPks() {
        String primaryKeyColumn = Optional.ofNullable(table.primaryKeyField())
                .map(HasColumns::columns)
                .map(cols -> cols.get(0))
                .map(Column::sqlName)
                .orElse(null);

        Map<String, String> context = EasyMaps.asMap(
            "$pk_column", primaryKeyColumn
        );

        if (table.isPrimaryKeyInt()) {
            appendCode("""
            @Override
            public @Nonnull IntArrayList fetchPks(@Nonnull Filter filter) {
                String query = "SELECT $pk_column FROM $table_sql\\n" + filter.repr();
                try {
                    return runner().fetchIntColumn(() -> runner().prepareQuery(query, filter.args()));
                } catch (SQLException e) {
                    throw new QueryException("Failed to fetch by PKs in $TableClass", query, filter.args(), e);
                }
            }\n
            """, EasyMaps.merge(context, mainContext, pkContext));
        }
        if (table.isPrimaryKeyLong()) {
            appendCode("""
            @Override
            public @Nonnull LongArrayList fetchPks(@Nonnull Filter filter) {
                String query = "SELECT $pk_column FROM $table_sql\\n" + filter.repr();
                try {
                    return runner().fetchLongColumn(() -> runner().prepareQuery(query, filter.args()));
                } catch (SQLException e) {
                    throw new QueryException("Failed to fetch by PKs in $TableClass", query, filter.args(), e);
                }
            }\n
            """, EasyMaps.merge(context, mainContext, pkContext));
        }
    }

    private void keyOf() {
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

    private void iterator() {
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

    private void insert() {
        Snippet query = new InsertMaker(InsertMaker.Ignore.DEFAULT).makeAll(table);
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

    private void insertIgnore() {
        Map<String, String> context = Map.of(
            "$model_id_assert", AssertModelIdMaker.makeAssert("$model_param", table).join(),
            "$sql_query_literal1", wrapAsStringLiteral(new InsertMaker(InsertMaker.Ignore.IGNORE).makeAll(table), INDENT3),
            "$sql_query_literal2", wrapAsStringLiteral(new InsertMaker(InsertMaker.Ignore.OR_IGNORE).makeAll(table), INDENT3)
        );

        appendCode("""
        @Override
        public int insertIgnore(@Nonnull $ModelClass $model_param) {
            $model_id_assert
            String query = switch (engine()) {
                case MySQL, H2 -> $sql_query_literal1;
                case SQLite -> $sql_query_literal2;
                default -> throw new UnsupportedOperationException(
                    "Insert-ignore unsupported for %s. Use insert() inside try-catch block".formatted(engine()));
            };
            try {
                return runner().runUpdate(query, valuesForInsert($model_param));
            } catch (SQLException e) {
                throw new QueryException("Failed to insert entity into $TableClass", query, $model_param, e);
           }
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private void valuesForInsert() {
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

    private void insertAutoIncPk() {
        if (!table.isPrimaryKeyInt() && !table.isPrimaryKeyLong()) {
            return;
        }

        Snippet query = new InsertMaker(InsertMaker.Ignore.DEFAULT)
            .make(table, table.columns(Predicate.not(TableField::isPrimaryKey)));
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

    private void valuesForInsertAutoIncPk() {
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

    private void updateWhere() {
        Snippet query = new Snippet()
                .withLines(UpdateMaker.make(table, table.columns(Predicate.not(TableField::isPrimaryKey))));
        Map<String, String> context = EasyMaps.asMap(
            "$sql_query_literal", wrapAsStringLiteral(query, INDENT2)
        );

        appendCode("""
        @Override
        public int updateWhere(@Nonnull $ModelClass $model_param, @Nonnull Where where) {
            String query = $sql_query_literal + where.repr();
            List<Object> args = valuesForUpdateWhere($model_param, where.args());
            try {
                return runner().runUpdate(query, args);
            } catch (SQLException e) {
                throw new QueryException("Failed to update entities in $TableClass by a filter", query, args, e);
            }
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private void valuesForUpdateWhere() {
        List<TableField> nonPrimary = table.fields().stream().filter(Predicate.not(TableField::isPrimaryKey)).toList();
        ValuesArrayMaker maker = new ValuesArrayMaker("$model_param", nonPrimary);
        Map<String, String> context = Map.of(
            "$array_init", maker.makeInitValues().join(linesJoiner(INDENT2)),
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT1, true))
        );

        appendCode("""
        protected static @Nonnull List<Object> valuesForUpdateWhere(@Nonnull $ModelClass $model_param, @Nonnull Args args) {
            Object[] values = valuesForUpdateWhere($model_param);
            if (args.isEmpty()) {
                return Arrays.asList(values);
            }
            List<Object> result = new ArrayList<>(values.length + args.size());
            result.addAll(Arrays.asList(values));
            result.addAll(args.asList());
            return result;
        }
        
        protected static @Nonnull Object[] valuesForUpdateWhere(@Nonnull $ModelClass $model_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private void updateByPk() {
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
                return runner().runUpdate(query, valuesForUpdateByPk($model_param));
            } catch (SQLException e) {
                throw new QueryException("Failed to update entity in $TableClass by PK", query, $model_param, e);
            }
        }\n
        """, EasyMaps.merge(mainContext, pkContext, context));
    }

    private void valuesForUpdateByPk() {
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
        protected static @Nonnull Object[] valuesForUpdateByPk(@Nonnull $ModelClass $model_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private void insertData() {
        appendCode("""
        @Override
        public int insertData(@Nonnull EntityData<?> data) {
            Collection<? extends Column> columns = data.columns();
            assert columns.size() > 0 : "Entity data contains empty columns: " + data;
            
            String query = makeInsertQueryForColumns(columns);
            try (PreparedStatement statement = runner().prepareQuery(query)) {
                data.provideValues(statement);
                return statement.executeUpdate();
            } catch (SQLException e) {
                throw new QueryException("Failed to insert entity data into $TableClass", query, data, e);
            }
        }
        
        protected static @Nonnull String makeInsertQueryForColumns(@Nonnull Collection<? extends Column> columns) {
            String columnsSql = columns.stream().map(Column::name).collect(Collectors.joining(", "));
            String valuesSql = "?,".repeat(columns.size() - 1);
            return "INSERT INTO $table_sql (" + columnsSql + ")\\n" +
                   "VALUES (" + valuesSql + "?)\\n";
        }\n
        """, mainContext);
    }

    private void updateDataWhere() {
        appendCode("""
        @Override
        public int updateDataWhere(@Nonnull EntityData<?> data, @Nonnull Where where) {
            Collection<? extends Column> columns = data.columns();
            assert columns.size() > 0 : "Entity data contains empty columns: " + data;
    
            String query = makeUpdateQueryForColumns(columns, where);
            try (PreparedStatement statement = runner().prepareQuery(query)) {
                data.provideValues(statement);
                return statement.executeUpdate();
            } catch (SQLException e) {
                throw new QueryException("Failed to update entities data in $TableClass by a filter", query, data, e);
            }
        }
        
        protected static @Nonnull String makeUpdateQueryForColumns(@Nonnull Collection<? extends Column> columns, @Nonnull Where where) {
            String valuesSql = columns.stream().map(column -> column.name() + "=?").collect(Collectors.joining(", ", "", "\\n"));
            return "UPDATE $table_sql\\n" +
                   "SET " + valuesSql + where.repr();
        }\n
        """, mainContext);
    }

    private void insertBatch() {
        Snippet query = new InsertMaker(InsertMaker.Ignore.DEFAULT).makeAll(table);
        Map<String, String> context = Map.of(
            // TODO[minor]: add an assert for batch
            // "$model_id_assert", AssertModelIdMaker.makeAssert("$model_param", table).join(),
            "$sql_query_literal", wrapAsStringLiteral(query, INDENT2)
        );

        appendCode("""
        @Override
        public int[] insertBatch(@Nonnull Collection<? extends $ModelClass> batch) {
            String query = $sql_query_literal;
            try {
                return runner().runUpdateBatch(query, batch.stream().map($TableClass::valuesForInsert).toList());
            } catch (SQLException e) {
                throw new QueryException("Failed to insert a batch of entities into $TableClass", query, batch, e);
            }
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private void updateWhereBatch() {
        Snippet query = new Snippet()
                .withLines(UpdateMaker.make(table, table.columns(Predicate.not(TableField::isPrimaryKey))));
        Map<String, String> context = EasyMaps.asMap(
            "$sql_query_literal", wrapAsStringLiteral(query, INDENT2)
        );

        appendCode("""
        @Override
        public int[] updateWhereBatch(@Nonnull Collection<? extends $ModelClass> batch, @Nonnull Contextual<Where, $ModelClass> where) {
            String query = $sql_query_literal + where.repr();
            try {
                return runner().runUpdateBatch(
                    query,
                    batch.stream().map(entity -> valuesForUpdateWhere(entity, where.resolveQueryArgs(entity))).toList()
                );
            } catch (SQLException e) {
                throw new QueryException("Failed to update a batch of entities in $TableClass by a filter", query, batch, e);
            }
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private void insertDataBatch() {
        appendCode("""
        @Override
        public int[] insertDataBatch(@Nonnull BatchEntityData<?> batchData) {
            Collection<? extends Column> columns = batchData.columns();
            assert columns.size() > 0 : "Entity data contains empty columns: " + batchData;
    
            String query = makeInsertQueryForColumns(columns);
            try (PreparedStatement statement = runner().prepareQuery(query)) {
                batchData.provideBatchValues(statement, null);
                return statement.executeBatch();
            } catch (SQLException e) {
                throw new QueryException("Failed to insert a batch of entity data into $TableClass", query, batchData, e);
            }
        }\n
        """, mainContext);
    }

    private void updateDataWhereBatch() {
        appendCode("""
        @Override
        public <B> int[] updateDataWhereBatch(@Nonnull BatchEntityData<B> batchData, @Nonnull Contextual<Where, B> where) {
            Collection<? extends Column> columns = batchData.columns();
            assert columns.size() > 0 : "Entity data contains empty columns: " + batchData;
    
            String query = makeUpdateQueryForColumns(columns, where.query());
            try (PreparedStatement statement = runner().prepareQuery(query)) {
                batchData.provideBatchValues(statement, where);
                return statement.executeBatch();
            } catch (SQLException e) {
                throw new QueryException("Failed to update batch of entity data in $TableClass by a filter", query, batchData, e);
            }
        }\n
        """, mainContext);
    }

    private void deleteByPk() {
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

    private void deleteWhere() {
        Snippet query = new Snippet().withLines(DeleteMaker.make(table));
        Map<String, String> context = EasyMaps.asMap(
            "$sql_query_literal", wrapAsTextBlock(query, INDENT2)
        );

        appendCode("""
        @Override
        public int deleteWhere(@Nonnull Where where) {
            String query = $sql_query_literal + where.repr();
            try {
               return runner().runUpdate(query, where.args());
           } catch (SQLException e) {
               throw new QueryException("Failed to delete entities in $TableClass by a filter", query, where.args(), e);
           }
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private void fromRow() {
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

    private void m2m() {
        if (!table.isM2M()) {
            return;
        }

        Map<String, String> context = m2mContext();

        appendCode("""
        @Override
        public boolean exists(@Nonnull $left_index_wrap leftIndex, @Nonnull $right_index_wrap rightIndex) {
            return exists(Where.hardcoded("$left_fk_sql = ? AND $right_fk_sql = ?", Args.of(leftIndex, rightIndex)));
        }
        
        @Override
        public int countRights(@Nonnull $left_index_wrap leftIndex) {
            String sql = "$right_pk_sql IN (SELECT $right_fk_sql FROM $table_sql WHERE $left_fk_sql = ?)";
            return rightsTable.count(Where.hardcoded(sql, Args.of(leftIndex)));
        }
        
        @Override
        public @Nonnull ResultSetIterator<$right_entity> iterateRights(@Nonnull $left_index_wrap leftIndex) {
            String sql = "$right_pk_sql IN (SELECT $right_fk_sql FROM $table_sql WHERE $left_fk_sql = ?)";
            return rightsTable.iterator(Where.hardcoded(sql, Args.of(leftIndex)));
        }
        
        @Override
        public int countLefts(@Nonnull $right_index_wrap rightIndex) {
            String sql = "$left_pk_sql IN (SELECT $left_fk_sql FROM $table_sql WHERE $right_fk_sql = ?)";
            return leftsTable.count(Where.hardcoded(sql, Args.of(rightIndex)));
        }
        
        @Override
        public @Nonnull ResultSetIterator<$left_entity> iterateLefts(@Nonnull $right_index_wrap rightIndex) {
            String sql = "$left_pk_sql IN (SELECT $left_fk_sql FROM $table_sql WHERE $right_fk_sql = ?)";
            return leftsTable.iterator(Where.hardcoded(sql, Args.of(rightIndex)));
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private @NotNull Map<String, String> m2mContext() {
        if (!table.isM2M()) {
            return Map.of();
        }

        ForeignTableField leftField = table.m2mLeftFieldOrDie();
        ForeignTableField rightField = table.m2mRightFieldOrDie();

        TableArch leftTable = leftField.getForeignTable();
        TableArch rightTable = rightField.getForeignTable();

        TableField leftTablePrimaryField = requireNonNull(leftTable.primaryKeyField());
        TableField rightTablePrimaryField = requireNonNull(rightTable.primaryKeyField());

        return EasyMaps.asMap(
            "$left_table_sql", leftTable.sqlName(),
            "$left_index_native", Naming.shortCanonicalJavaName(leftTablePrimaryField.javaType()),
            "$left_index_wrap", Naming.shortCanonicalJavaName(Primitives.wrap(leftTablePrimaryField.javaType())),
            "$left_pk_sql", leftTablePrimaryField.columns().get(0).sqlName(),
            "$left_fk_sql", leftField.columns().get(0).sqlName(),
            "$left_entity", Naming.shortCanonicalJavaName(leftTable.modelClass()),

            "$right_table_sql", rightTable.sqlName(),
            "$right_index_native", Naming.shortCanonicalJavaName(rightTablePrimaryField.javaType()),
            "$right_index_wrap", Naming.shortCanonicalJavaName(Primitives.wrap(rightTablePrimaryField.javaType())),
            "$right_pk_sql", rightTablePrimaryField.columns().get(0).sqlName(),
            "$right_fk_sql", rightField.columns().get(0).sqlName(),
            "$right_entity", Naming.shortCanonicalJavaName(rightTable.modelClass())
        );
    }

    private void internalMeta() {
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

    private void columnsEnum() {
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

    private void tableMeta() {
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
                return "new ColumnMeta(OwnColumn.%s, %s.class, %s, %s)".formatted(sqlName, type, primaryKey, foreignKey);
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
