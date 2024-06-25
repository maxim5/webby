package io.spbx.webby.testing;

import com.google.common.flogger.FluentLogger;
import io.spbx.orm.api.Engine;
import io.spbx.webby.db.sql.SqlSettings;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.logging.Level;

import static org.junit.Assume.assumeTrue;

public class TestingProps {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static void assumePropIfSet(@NotNull String name, @NotNull String expected) {
        assumePropIfSet(name, expected::equals);
    }

    public static void assumePropIfSet(@NotNull String name, @NotNull Predicate<String> condition) {
        String value = System.getProperty(name);
        assumeTrue("Skipping the test because the property is set and does not match test assumption. " +
                   "Provided prop: %s=`%s`".formatted(name, value),
                   value == null || condition.test(value));
    }

    public static @NotNull SqlSettings propsSqlSettings() {
        String engineValue = System.getProperty("test.sql.engine");
        if (engineValue != null) {
            Engine engine = Engine.fromJdbcType(engineValue.toLowerCase());
            assert engine != Engine.Unknown : "Failed to detect SQL engine: " + engineValue;
            log.at(Level.INFO).log("[SQL] Detected engine: %s", engine);
            return SqlSettings.inMemoryForDevOnly(engine);
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
