package io.webby.testing.ext;

import io.webby.orm.api.Connector;
import io.webby.orm.api.TableMeta;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.orm.api.query.DropTableQuery;
import io.webby.orm.api.query.TruncateTableQuery;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

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

    public static @NotNull SqlCleanupExtension of(@NotNull Connector connector, @NotNull TableMeta @NotNull ... tables) {
        return new SqlCleanupExtension(connector, List.of(tables));
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        connector.runner().adminTx().run(admin -> {
            for (TableMeta table : tables) {
                admin.ignoringForeignKeyChecks().dropTable(DropTableQuery.of(table).ifExists().cascade());
                admin.createTable(CreateTableQuery.of(table).ifNotExists());
            }
        });
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        connector.runner().adminTx().run(admin -> {
            for (TableMeta table : tables) {
                admin.ignoringForeignKeyChecks().truncateTable(TruncateTableQuery.of(table));
            }
        });
    }
}
