package io.webby.orm.api;

import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CheckReturnValue;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.orm.api.query.DropTableQuery;
import io.webby.orm.api.query.HardcodedSelectQuery;
import io.webby.util.base.Unchecked;
import io.webby.util.func.ThrowRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class DbAdmin {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Connector connector;

    public DbAdmin(@NotNull Connector connector) {
        this.connector = connector;
    }

    public static @NotNull DbAdmin ofFixed(@NotNull Connection connection) {
        return new DbAdmin(() -> connection);
    }

    public static @NotNull DbAdmin ofFixed(@NotNull QueryRunner runner) {
        return new DbAdmin(runner::connection);
    }

    public @Nullable String getDatabase() {
        try {
            String catalog = connector.connection().getCatalog();
            if (catalog != null) {
                return catalog;
            }
        } catch (SQLException e) {
            return Unchecked.rethrow(e);
        }

        return switch (engine()) {
            case MySQL -> runner().runAndGetString(HardcodedSelectQuery.of("SELECT DATABASE()"));
            case SQLite ->
                runner().runAndGetString(HardcodedSelectQuery.of("SELECT name FROM pragma_database_list LIMIT 1"));
            default -> throw new UnsupportedOperationException("Failed to get the current database in " + engine());
        };
    }

    public void createTable(@NotNull CreateTableQuery query) throws SQLException {
        log.at(Level.INFO).log("Creating SQL table: `%s`...", query.tableName());
        int rows = runner().runUpdate(query);
        log.at(Level.FINER).log("Query OK, %d rows affected", rows);
    }

    @CheckReturnValue
    public @NotNull CreateTableRunnable createTable(@NotNull TableMeta meta) {
        return new CreateTableRunnable(CreateTableQuery.of(meta, engine()));
    }

    public void dropTable(@NotNull DropTableQuery query) throws SQLException {
        log.at(Level.INFO).log("Deleting SQL table: `%s`...", query.tableName());
        int rows = runner().runUpdate(query);
        log.at(Level.FINER).log("Query OK, %d rows affected", rows);
    }

    @CheckReturnValue
    public @NotNull DropTableRunnable dropTable(@NotNull TableMeta meta) {
        return new DropTableRunnable(DropTableQuery.bestEffortOf(meta, engine()));
    }

    private @NotNull Engine engine() {
        return connector.engine();
    }

    private @NotNull QueryRunner runner() {
        return connector.runner();
    }

    @CheckReturnValue
    public class CreateTableRunnable implements ThrowRunnable<SQLException> {
        private final CreateTableQuery.Builder builder;

        public CreateTableRunnable(@NotNull CreateTableQuery.Builder builder) {
            this.builder = builder;
        }

        public @NotNull CreateTableRunnable ifNotExists() {
            builder.ifNotExists();
            return this;
        }

        @Override
        public void run() throws SQLException {
            createTable(builder.build());
        }
    }

    @CheckReturnValue
    public class DropTableRunnable implements ThrowRunnable<SQLException> {
        private final DropTableQuery.Builder builder;

        public DropTableRunnable(@NotNull DropTableQuery.Builder builder) {
            this.builder = builder;
        }

        public @NotNull DropTableRunnable ifExists() {
            builder.ifExists();
            return this;
        }

        public @NotNull DropTableRunnable cascade() {
            builder.cascade();
            return this;
        }

        @Override
        public void run() throws SQLException {
            dropTable(builder.build());
        }
    }
}
