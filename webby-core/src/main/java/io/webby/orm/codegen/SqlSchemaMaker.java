package io.webby.orm.codegen;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import io.webby.orm.api.TableMeta.ColumnMeta;
import io.webby.util.base.Unchecked;
import io.webby.util.collect.EasyMaps;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;
import java.util.stream.Collectors;

import static io.webby.util.base.EasyCast.castAny;

public class SqlSchemaMaker {
    public static @NotNull String makeCreateTableQuery(@NotNull Engine engine,
                                                       @NotNull Class<? extends BaseTable<?>> tableClass) {
        try {
            TableMeta meta = castAny(tableClass.getField("META").get(null));
            return makeCreateTableQuery(engine, meta);
        } catch (Throwable e) {
            return Unchecked.rethrow(e);
        }
    }

    public static @NotNull String makeCreateTableQuery(@NotNull BaseTable<?> table) {
        return makeCreateTableQuery(table.engine(), table.meta());
    }

    public static @NotNull String makeCreateTableQuery(@NotNull Engine engine, @NotNull TableMeta meta) {
        SchemaSupport support = SchemaSupport.of(engine);
        Snippet snippet = new Snippet();

        meta.sqlColumns().forEach(column -> {
            String definition = SnippetLine.of(
                "%s %s".formatted(column.name(), support.columnTypeFor(column)),
                column.isNotNull() ? support.inlineNotNull(column) : "",
                column.primaryKey().isSingle() ? support.inlinePrimaryKeyFor(column) : "",
                column.primaryKey().isSingle() ? support.inlineAutoIncrementFor(column) : "",
                column.unique().isSingle() ? support.inlineUniqueFor(column) : "",
                column.hasDefault() ? support.inlineDefaultFor(column) : ""
            ).joinNonEmpty(" ");
            snippet.withLine(definition);
        });

        if (meta.primaryKeys().isComposite()) {
            String columns = SnippetLine.from(meta.primaryKeys().columns()).join(", ");
            snippet.withLine("PRIMARY KEY (%s)".formatted(columns));
        }

        meta.unique().forEach(constraint -> {
            if (constraint.isComposite()) {
                String columns = SnippetLine.from(constraint.columns()).join(", ");
                snippet.withLine("UNIQUE (%s)".formatted(columns));
            }
        });

        return """
        CREATE TABLE IF NOT EXISTS %s (
            %s
        )
        """.formatted(meta.sqlTableName(), snippet.join(Collectors.joining(",\n    ")));
    }

    public static @NotNull String makeDropTableQuery(@NotNull BaseTable<?> table) {
        return makeDropTableQuery(table.meta());
    }

    public static @NotNull String makeDropTableQuery(@NotNull TableMeta meta) {
        return makeDropTableQuery(meta.sqlTableName());
    }

    public static @NotNull String makeDropTableQuery(@NotNull String table) {
        return "DROP TABLE IF EXISTS %s".formatted(table);
    }

    private interface SchemaSupport {
        @NotNull String columnTypeFor(@NotNull ColumnMeta columnMeta);

        default @NotNull String inlineNotNull(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isNotNull() ? "NOT NULL" : "";
        }

        default @NotNull String inlineUniqueFor(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isUnique() ? "UNIQUE" : "";
        }

        default @NotNull String inlinePrimaryKeyFor(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isPrimaryKey() ? "PRIMARY KEY" : "";
        }

        default @NotNull String inlineAutoIncrementFor(@NotNull ColumnMeta columnMeta) {
            return isAutoIncrement(columnMeta) ? "AUTO_INCREMENT" : "";
        }

        static boolean isAutoIncrement(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isPrimaryKey() && (columnMeta.type() == int.class || columnMeta.type() == long.class);
        }

        default @NotNull String inlineDefaultFor(@NotNull ColumnMeta column) {
            return column.hasDefault() && !isAutoIncrement(column) ? "DEFAULT (%s)".formatted(column.defaultValue()) : "";
        }

        static @NotNull SchemaSupport of(@NotNull Engine engine) {
            return switch (engine) {
                case SQLite -> new SqliteSchemaSupport();
                case MySQL -> new MySqlSchemaSupport();
                case H2 -> new H2SchemaSupport();
                default -> throw new IllegalArgumentException("Engine not supported for table creation: " + engine);
            };
        }
    }

    private abstract static class DefaultSchemaSupport implements SchemaSupport {
        protected static final Map<Class<?>, String> DEFAULT_DATA_TYPES = EasyMaps.asMap(
            boolean.class, "BOOLEAN",
            byte.class, "TINYINT",
            short.class, "SMALLINT",
            int.class, "INTEGER",
            long.class, "BIGINT",
            float.class, "REAL",
            double.class, "DOUBLE",
            String.class, "VARCHAR",
            byte[].class, "BLOB",
            Time.class, "TIME",
            Date.class, "DATE",
            Timestamp.class, "TIMESTAMP"
        );

        protected static boolean isKey(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isPrimaryKey() || columnMeta.isForeignKey();
        }
    }

    private static class SqliteSchemaSupport extends DefaultSchemaSupport {
        private static final Map<Class<?>, String> SQLITE_DATA_TYPES = EasyMaps.asMap(
            float.class, "REAL",
            double.class, "REAL",
            String.class, "VARCHAR",
            byte[].class, "BLOB"
        );
        private static final Map<Class<?>, String> SQLITE_PK_DATA_TYPES = EasyMaps.merge(SQLITE_DATA_TYPES, EasyMaps.asMap(
            byte[].class, "VARCHAR"
        ));

        @Override
        public @NotNull String columnTypeFor(@NotNull ColumnMeta columnMeta) {
            return (isKey(columnMeta) ? SQLITE_PK_DATA_TYPES : SQLITE_DATA_TYPES).getOrDefault(columnMeta.type(), "INTEGER");
        }

        @Override
        public @NotNull String inlineAutoIncrementFor(@NotNull ColumnMeta columnMeta) {
            return "";  // Not recommended by https://www.sqlite.org/autoinc.html
        }
    }

    private static class MySqlSchemaSupport extends DefaultSchemaSupport {
        private static final Map<Class<?>, String> MYSQL_DATA_TYPES = EasyMaps.merge(DEFAULT_DATA_TYPES, EasyMaps.asMap(
            String.class, "VARCHAR(4096)",
            byte[].class, "BLOB",
            Timestamp.class, "TIMESTAMP(3)"
        ));
        // https://stackoverflow.com/questions/29292353/whats-the-difference-between-varchar-binary-and-varbinary-in-mysql
        private static final Map<Class<?>, String> MYSQL_PK_DATA_TYPES = EasyMaps.merge(MYSQL_DATA_TYPES, EasyMaps.asMap(
            String.class, "VARCHAR(255)",
            byte[].class, "VARBINARY(255)"
        ));

        @Override
        public @NotNull String columnTypeFor(@NotNull ColumnMeta columnMeta) {
            return (isKey(columnMeta) ? MYSQL_PK_DATA_TYPES : MYSQL_DATA_TYPES).get(columnMeta.type());
        }
    }

    private static class H2SchemaSupport extends DefaultSchemaSupport {
        private static final Map<Class<?>, String> H2_PK_DATA_TYPES = EasyMaps.merge(DEFAULT_DATA_TYPES, EasyMaps.asMap(
            byte[].class, "VARCHAR"
        ));

        @Override
        public @NotNull String columnTypeFor(@NotNull ColumnMeta columnMeta) {
            return (isKey(columnMeta) ? H2_PK_DATA_TYPES : DEFAULT_DATA_TYPES).get(columnMeta.type());
        }

        @Override
        public @NotNull String inlineUniqueFor(@NotNull ColumnMeta columnMeta) {
            // Index on BLOB or CLOB column not supported
            return !columnMeta.isUnique() || columnMeta.type() == byte[].class ? "" : "UNIQUE";
        }
    }
}
