package io.webby.auth;

import com.google.inject.Injector;
import io.webby.app.AppSettings;
import io.webby.auth.session.SessionStore;
import io.webby.auth.user.UserStore;
import io.webby.db.sql.SqlSettings;
import io.webby.testing.Testing;
import io.webby.testing.TestingStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;

// FIX[major]: transform into an extension. A scenario needs to be a class param for that.
public abstract class BaseCoreIntegrationTest {
    @AfterEach
    protected void tearDown() {
        Testing.Internals.terminate();
    }

    protected static @NotNull Injector startup(@NotNull Scenario scenario, @NotNull SqlSettings sqlSettings) {
        AppSettings settings = Testing.defaultAppSettings();
        settings.setProperty("user.id.generator.random.enabled", scenario.isRandomIdsEnabled());
        settings.setProperty("session.id.generator.random.enabled", scenario.isRandomIdsEnabled());
        settings.modelFilter().setCommonPackageOf(Testing.AUTH_MODELS);
        switch (scenario) {
            case SQL ->
                settings.storageSettings().enableSql(sqlSettings);
            case KEY_VALUE_RANDOM_ID, KEY_VALUE_AUTO_ID ->
                settings.storageSettings().enableKeyValue(TestingStorage.KEY_VALUE_DEFAULT);
        }
        return Testing.testStartup(settings);
    }

    protected static @NotNull UserStore getUserStore() {
        return Testing.Internals.getInstance(UserStore.class);
    }

    protected static @NotNull SessionStore getSessionStore() {
        return Testing.Internals.getInstance(SessionStore.class);
    }

    protected enum Scenario {
        SQL,
        KEY_VALUE_RANDOM_ID,
        KEY_VALUE_AUTO_ID;

        public boolean isRandomIdsEnabled() {
            return this == KEY_VALUE_RANDOM_ID;
        }
    }
}
