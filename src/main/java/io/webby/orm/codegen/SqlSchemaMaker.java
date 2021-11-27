package io.webby.orm.codegen;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import io.webby.util.base.Rethrow;
import io.webby.util.collect.EasyMaps;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
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
            return Rethrow.rethrow(e);
        }
    }

    public static @NotNull String makeCreateTableQuery(@NotNull Engine engine, @NotNull BaseTable<?> table) {
        return makeCreateTableQuery(engine, table.meta());
    }

    public static @NotNull String makeCreateTableQuery(@NotNull Engine engine, @NotNull TableMeta meta) {
        String tableName = meta.sqlTableName();
        List<TableMeta.ColumnMeta> columns = meta.sqlColumns();
        String definitions = columns.stream().map(column -> {
            boolean isPrimaryKey = column.isPrimaryKey();
            String sqlType = sqlTypeFor(column, engine);
            String def = "%s %s".formatted(column.name(), sqlType);
            if (isPrimaryKey) {
                return "%s %s %s".formatted(def, "PRIMARY KEY", sqlAutoIncrement(column, engine)).trim();
            }
            return def;
        }).collect(Collectors.joining(",\n    "));

        return """
        CREATE TABLE IF NOT EXISTS %s (
            %s
        )
        """.formatted(tableName, definitions);
    }

    public static @NotNull String makeCreateTableQuery(@NotNull Engine engine,
                                                       @NotNull Class<? extends BaseTable<?>> ... tableClasses) {
        return Arrays.stream(tableClasses)
                .map(tableClass -> makeCreateTableQuery(engine, tableClasses))
                .collect(Collectors.joining(";\n"));
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

    private static @NotNull String sqlTypeFor(@NotNull TableMeta.ColumnMeta columnMeta, @NotNull Engine engine) {
        Class<?> columnType = columnMeta.type();
        if (columnMeta.isPrimaryKey() && columnType == byte[].class) {
            columnType = String.class;  // Exception: BLOB PRIMARY KEY -> VARCHAR PRIMARY KEY
        }
        return switch (engine) {
            case H2 -> DEFAULT_DATA_TYPES.get(columnType);
            case SQLite -> {
                if (columnType == String.class) {
                    yield "VARCHAR";
                }
                if (columnType == byte[].class) {
                    yield "BLOB";
                }
                yield "INTEGER";
            }
            default -> throw new IllegalArgumentException("Engine not supported for table creation: " + engine);
        };
    }

    private static @NotNull String sqlAutoIncrement(@NotNull TableMeta.ColumnMeta columnMeta, @NotNull Engine engine) {
        if (columnMeta.isPrimaryKey() && (columnMeta.type() == int.class || columnMeta.type() == long.class)) {
            return switch (engine) {
                case H2 -> "AUTO_INCREMENT";
                case SQLite -> "";  // Not recommended by https://www.sqlite.org/autoinc.html
                default -> throw new IllegalArgumentException("Engine not supported for table creation: " + engine);
            };
        }
        return "";
    }
}
