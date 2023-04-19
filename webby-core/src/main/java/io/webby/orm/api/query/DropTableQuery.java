package io.webby.orm.api.query;

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

    public static @NotNull Builder bestEffortOf(@NotNull TableMeta meta, @NotNull Engine engine) {
        return new Builder(meta.sqlTableName(), engine);
    }

    public static class Builder {
        private final String tableName;
        private final Engine engine;
        private boolean ifExists;
        private boolean cascade;

        Builder(@NotNull String tableName) {
            this.tableName = tableName;
            this.engine = Engine.Unknown;
        }

        Builder(@NotNull String tableName, @NotNull Engine engine) {
            this.tableName = tableName;
            this.engine = engine;
        }

        public @NotNull Builder ifExists() {
            ifExists = true;
            return this;
        }

        public @NotNull Builder cascade() {
            if (engine != Engine.SQLite) {
                cascade = true;
            }
            return this;
        }

        public @NotNull DropTableQuery build() {
            return new DropTableQuery(buildToQuery(), tableName);
        }

        private @NotNull String buildToQuery() {
            StringBuilder builder = new StringBuilder();
            builder.append("DROP TABLE ");
            if (ifExists) {
                builder.append("IF EXISTS ");
            }
            builder.append(tableName);
            if (cascade) {
                builder.append(" CASCADE");
            }
            return builder.toString();
        }
    }
}
