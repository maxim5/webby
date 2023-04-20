package io.webby.orm.api.query;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

public class TruncateTableQuery extends Unit implements DataDefinitionQuery {
    private final String tableName;

    private TruncateTableQuery(@NotNull String query, @NotNull String tableName) {
        super(query);
        this.tableName = tableName;
    }

    public @NotNull String tableName() {
        return tableName;
    }

    public static @NotNull Builder of(@NotNull String tableName) {
        return new Builder(tableName);
    }

    public static @NotNull Builder of(@NotNull TableMeta meta) {
        return of(meta.sqlTableName());
    }

    public static @NotNull Builder of(@NotNull BaseTable<?> table) {
        return of(table.meta());
    }

    public static class Builder {
        private final String tableName;

        Builder(@NotNull String tableName) {
            this.tableName = tableName;
        }

        public @NotNull TruncateTableQuery build(@NotNull Engine engine) {
            String query = switch (engine) {
                case SQLite -> "DELETE FROM %s".formatted(tableName);
                default -> "TRUNCATE TABLE %s".formatted(tableName);
            };
            return new TruncateTableQuery(query, tableName);
        }
    }
}
