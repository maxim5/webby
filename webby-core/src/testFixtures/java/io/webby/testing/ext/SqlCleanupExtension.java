package io.webby.testing.ext;

import io.webby.orm.api.Connector;
import io.webby.orm.api.DbAdmin;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.SQLException;
import java.util.List;

public class SqlCleanupExtension implements BeforeAllCallback, BeforeEachCallback {
    private final Connector connector;
    private final List<TableMeta> tables;

    public SqlCleanupExtension(@NotNull Connector connector, @NotNull List<TableMeta> tables) {
        this.connector = connector;
        this.tables = tables;
    }

    public static @NotNull SqlCleanupExtension of(@NotNull Connector connector, @NotNull TableMeta table) {
        return new SqlCleanupExtension(connector, List.of(table));
    }

    @Override
    public void beforeAll(ExtensionContext context) throws SQLException {
        connector.runner().runInTransaction(runner -> {
            DbAdmin admin = DbAdmin.ofFixed(runner);
            for (TableMeta table : tables) {
                admin.dropTable(table).ifExists().cascade().run();
                admin.createTable(table).ifNotExists().run();
            }
        });
    }

    @Override
    public void beforeEach(ExtensionContext context) throws SQLException {
        connector.runner().runInTransaction(runner -> {
            for (TableMeta table : tables) {
                runner.runUpdate("DELETE FROM %s".formatted(table.sqlTableName()));
            }
        });
    }
}
