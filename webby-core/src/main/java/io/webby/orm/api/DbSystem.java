package io.webby.orm.api;

import io.webby.orm.api.query.Args;
import io.webby.orm.api.query.HardcodedSelectQuery;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class DbSystem {
    public static @Nullable String getDatabase(@NotNull BaseTable<?> table) {
        try {
            String catalog = table.runner().connection().getCatalog();
            if (catalog != null) {
                return catalog;
            }
        } catch (SQLException e) {
            return Unchecked.rethrow(e);
        }

        return switch (table.engine()) {
            case MySQL -> table.runner().runAndGetString(HardcodedSelectQuery.of("SELECT DATABASE()"));
            case SQLite ->
                table.runner().runAndGetString(HardcodedSelectQuery.of("SELECT name FROM pragma_database_list LIMIT 1"));
            default -> throw new UnsupportedOperationException("Failed to get the current database in " + table.engine());
        };
    }

    public static @Nullable LocalDateTime getLastUpdateTime(@NotNull BaseTable<?> table, boolean forceCacheUpdate) {
        String tableName = table.meta().sqlTableName();
        return switch (table.engine()) {
            case MySQL -> {
                if (forceCacheUpdate) {
                    // - To update cached values at any time for a given table, use ANALYZE TABLE. Source:
                    // https://dev.mysql.com/doc/refman/8.0/en/server-system-variables.html#sysvar_information_schema_stats_expiry
                    //
                    // Related bugs:
                    // https://bugs.mysql.com/bug.php?id=95374 (information_schema.TABLES.UPDATE_TIME is useless with innodb)
                    // https://bugs.mysql.com/bug.php?id=95407 (Information schema: update_time not working)
                    // https://bugs.mysql.com/bug.php?id=86170
                    //
                    // https://www.percona.com/blog/2020/01/03/inconsistent-table-information-in-mysql-8-0-information_schema/
                    table.runner().runAndGet(HardcodedSelectQuery.of("ANALYZE TABLE %s".formatted(tableName)));
                }

                String sql = """
                    SELECT UPDATE_TIME
                    FROM   information_schema.tables
                    WHERE TABLE_SCHEMA IN (SELECT DATABASE()) AND TABLE_NAME = ?
                """;
                yield (LocalDateTime) table.runner().runAndGet(HardcodedSelectQuery.of(sql, Args.of(tableName)));
            }
            default -> throw new UnsupportedOperationException("Failed to get the last update time in " + table.engine());
        };
    }
}
