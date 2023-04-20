package io.webby.orm.api;

import com.google.common.flogger.FluentLogger;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.orm.api.query.DropTableQuery;
import io.webby.orm.api.query.HardcodedSelectQuery;
import io.webby.orm.api.query.TruncateTableQuery;
import io.webby.util.base.Unchecked;
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

    public void createTable(@NotNull CreateTableQuery.Builder builder) throws SQLException {
        createTable(builder.build(engine()));
    }

    public void dropTable(@NotNull DropTableQuery query) throws SQLException {
        log.at(Level.INFO).log("Deleting SQL table: `%s`...", query.tableName());
        int rows = runner().runUpdate(query);
        log.at(Level.FINER).log("Query OK, %d rows affected", rows);
    }

    public void dropTable(@NotNull DropTableQuery.Builder builder) throws SQLException {
        dropTable(builder.build(engine()));
    }

    public void truncateTable(@NotNull TruncateTableQuery query) throws SQLException {
        log.at(Level.INFO).log("Truncating SQL table: `%s`...", query.tableName());
        int rows = runner().runUpdate(query);
        log.at(Level.FINER).log("Query OK, %d rows affected", rows);
    }

    public void truncateTable(@NotNull TruncateTableQuery.Builder builder) throws SQLException {
        truncateTable(builder.build(engine()));
    }

    private @NotNull Engine engine() {
        return connector.engine();
    }

    private @NotNull QueryRunner runner() {
        return connector.runner();
    }
}
