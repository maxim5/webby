package io.webby.db.sql;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.webby.app.Settings;
import io.webby.orm.api.Engine;
import io.webby.orm.codegen.SqlSchemaMaker;

import java.sql.SQLException;
import java.util.logging.Level;

import static io.webby.util.base.Rethrow.rethrow;

@Singleton
public class TableCreator {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private TableManager tableManager;

    public void createAllTablesIfNotExist() {
        assert settings.storageSettings().isSqlStorageEnabled() : "SQL storage is disabled";

        Engine engine = tableManager.connector().engine();
        tableManager.iterateRegisteredTables((meta, key, entity) -> {
            try {
                log.at(Level.FINE).log("Creating SQL table if not exists: `%s`...", meta.sqlTableName());
                String query = SqlSchemaMaker.makeCreateTableQuery(engine, meta);
                tableManager.connector().runner().runMultiUpdate(query);
            } catch (SQLException e) {
                rethrow(e);
            }
        });
    }
}
