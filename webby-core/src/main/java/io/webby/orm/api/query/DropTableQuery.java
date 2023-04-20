package io.webby.orm.api.query;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

public class DropTableQuery extends Unit implements DataDefinitionQuery {
    private final String tableName;

    private DropTableQuery(@NotNull String query, @NotNull String tableName) {
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
        private boolean ifExists;
        private boolean cascade;

        Builder(@NotNull String tableName) {
            this.tableName = tableName;
        }

        public @NotNull Builder ifExists() {
            ifExists = true;
            return this;
        }

        public @NotNull Builder cascade() {
            cascade = true;
            return this;
        }

        public @NotNull DropTableQuery build(@NotNull Engine engine) {
            StringBuilder builder = new StringBuilder();
            builder.append("DROP TABLE ");
            if (ifExists) {
                builder.append("IF EXISTS ");
            }
            builder.append(tableName);
            if (cascade && engine != Engine.SQLite) {
                builder.append(" CASCADE");
            }
            String query = builder.toString();
            return new DropTableQuery(query, tableName);
        }
    }
}
