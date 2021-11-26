package io.webby.orm.codegen;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
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
            String sqlType = sqlTypeFor(column.type(), engine);
            String def = "%s %s".formatted(column.name(), sqlType);
            if (column.isPrimaryKey()) {
                return def + " PRIMARY KEY";
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

    private static @NotNull String sqlTypeFor(@NotNull Class<?> columnType, @NotNull Engine engine) {
        if (engine == Engine.SQLite) {
            if (columnType == String.class) {
                return "TEXT";
            }
            if (columnType == byte[].class) {
                return "BLOB";
            }
            return "INTEGER";
        }

        throw new IllegalArgumentException("Engine not supported: " + engine);
    }
}
