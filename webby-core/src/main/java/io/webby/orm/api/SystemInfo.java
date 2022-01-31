package io.webby.orm.api;

import io.webby.orm.api.query.Args;
import io.webby.orm.api.query.HardcodedSelectQuery;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class SystemInfo {
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

    public static @Nullable LocalDateTime getLastUpdateTime(@NotNull BaseTable<?> table) {
        return switch (table.engine()) {
            case MySQL -> {
                String sql = """
                    SELECT UPDATE_TIME
                    FROM   information_schema.tables
                    WHERE TABLE_SCHEMA IN (SELECT DATABASE()) AND TABLE_NAME = ?
                """;
                yield (LocalDateTime) table.runner().runAndGet(
                    HardcodedSelectQuery.of(sql, Args.of(table.meta().sqlTableName()))
                );
            }
            default -> throw new UnsupportedOperationException("Failed to get the last update time in " + table.engine());
        };
    }
}
