package io.webby.testing;

import com.google.common.flogger.FluentLogger;
import io.webby.db.sql.SqlSettings;
import io.webby.orm.api.Engine;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class TestingProps {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static @NotNull SqlSettings parseSqlSettings() {
        String engineValue = System.getProperty("test.sql.engine");
        if (engineValue != null) {
            Engine engine = Engine.fromJdbcType(engineValue.toLowerCase());
            assert engine != Engine.Unknown : "Failed to detect SQL engine: " + engineValue;
            log.at(Level.INFO).log("[SQL] Detected engine: %s", engine);
            return SqlSettings.inMemoryNotForProduction(engine);
        }
        String url = System.getProperty("test.sql.url");
        if (url != null) {
            Engine engine = SqlSettings.parseEngineFromUrl(url);
            assert engine != Engine.Unknown : "Failed to detect SQL engine: " + url;
            log.at(Level.INFO).log("[SQL] Inferred engine: %s. Url: %s", engine, url);
            return new SqlSettings(url);
        }
        log.at(Level.INFO).log("[SQL] Test SQL properties not found. Using default SQLite in memory");
        return SqlSettings.SQLITE_IN_MEMORY;
    }
}
