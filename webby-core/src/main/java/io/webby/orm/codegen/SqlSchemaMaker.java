package io.webby.orm.codegen;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import io.webby.orm.api.query.Named;
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
        Snippet snippet = new Snippet();

        snippet.withLines(meta.sqlColumns().stream().map(column -> {
            String sqlType = sqlTypeFor(column, engine);
            return SnippetLine.of(
                "%s %s".formatted(column.name(), sqlType),
                column.primaryKey().isSingle() ? "PRIMARY KEY" : "",
                column.primaryKey().isSingle() ? sqlAutoIncrement(column, engine) : "",
                column.unique().isSingle() ? "UNIQUE" : ""
            ).joinNonEmpty(" ");
        }));

        if (meta.primaryKeys().isComposite()) {
            String columns = meta.primaryKeys().columns().stream().map(Named::name).collect(Collectors.joining(", "));
            snippet.withLine("PRIMARY KEY (%s)".formatted(columns));
        }

        meta.unique().forEach(constraint -> {
            if (constraint.isComposite()) {
                String columns = constraint.columns().stream().map(Named::name).collect(Collectors.joining(", "));
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

    private static final Map<Class<?>, String> DEFAULT_DATA_TYPES = EasyMaps.asMap(
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

    private static final Map<Class<?>, String> H2_PK_DATA_TYPES = EasyMaps.merge(DEFAULT_DATA_TYPES, EasyMaps.asMap(
        byte[].class, "VARCHAR"
    ));

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

    private static final Map<Class<?>, String> SQLITE_DATA_TYPES = EasyMaps.asMap(
        float.class, "REAL",
        double.class, "REAL",
        String.class, "VARCHAR",
        byte[].class, "BLOB"
    );
    private static final Map<Class<?>, String> SQLITE_PK_DATA_TYPES = EasyMaps.merge(SQLITE_DATA_TYPES, EasyMaps.asMap(
        byte[].class, "VARCHAR"
    ));

    private static @NotNull String sqlTypeFor(@NotNull TableMeta.ColumnMeta columnMeta, @NotNull Engine engine) {
        boolean isPrimaryKey = columnMeta.isPrimaryKey() || columnMeta.isForeignKey();
        Class<?> columnType = columnMeta.type();
        return switch (engine) {
            case H2 -> (isPrimaryKey ? H2_PK_DATA_TYPES : DEFAULT_DATA_TYPES).get(columnType);
            case MySQL -> (isPrimaryKey ? MYSQL_PK_DATA_TYPES : MYSQL_DATA_TYPES).get(columnType);
            case SQLite -> (isPrimaryKey ? SQLITE_PK_DATA_TYPES : SQLITE_DATA_TYPES).getOrDefault(columnType, "INTEGER");
            default -> throw new IllegalArgumentException("Engine not supported for table creation: " + engine);
        };
    }

    private static @NotNull String sqlAutoIncrement(@NotNull TableMeta.ColumnMeta columnMeta, @NotNull Engine engine) {
        if (columnMeta.isPrimaryKey() && (columnMeta.type() == int.class || columnMeta.type() == long.class)) {
            return switch (engine) {
                case H2, MySQL -> "AUTO_INCREMENT";
                case SQLite -> "";  // Not recommended by https://www.sqlite.org/autoinc.html
                default -> throw new IllegalArgumentException("Engine not supported for table creation: " + engine);
            };
        }
        return "";
    }
}
