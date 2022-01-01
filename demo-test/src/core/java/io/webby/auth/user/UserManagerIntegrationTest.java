package io.webby.auth.user;

import io.webby.app.AppSettings;
import io.webby.testing.Testing;
import io.webby.testing.TestingModels;
import io.webby.testing.TestingStorage;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.webby.db.model.IntAutoIdModel.AUTO_ID;
import static org.junit.jupiter.api.Assertions.*;

@Tag("sql")
public class UserManagerIntegrationTest {
    @RegisterExtension private static final SqlDbSetupExtension SQL_DB = SqlDbSetupExtension.fromProperties();

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void no_users(Scenario scenario) {
        SimpleUserManager userManager = startup(scenario);
        assertNull(userManager.findByUserId(0));
        assertNull(userManager.findByUserId(1));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void create_one_user(Scenario scenario) {
        SimpleUserManager userManager = startup(scenario);
        DefaultUser user = TestingModels.newUser(AUTO_ID, Instant.now().truncatedTo(ChronoUnit.MILLIS), UserAccess.Simple);
        int userId = userManager.createUserAutoId(user);
        assertEquals(userId, user.userId());
        assertTrue(userId > 0);
        assertEquals(user, userManager.findByUserId(userId));
        assertNull(userManager.findByUserId(0));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void invalid_user_with_id(Scenario scenario) {
        SimpleUserManager userManager = startup(scenario);
        DefaultUser user = TestingModels.newUserNow(1, UserAccess.Simple);
        assertThrows(AssertionError.class, () -> userManager.createUserAutoId(user));
    }

    private static @NotNull SimpleUserManager startup(@NotNull Scenario scenario) {
        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(DefaultUser.class);
        switch (scenario) {
            case SQL -> settings.storageSettings().enableSql(SQL_DB.settings()).enableKeyValue(TestingStorage.KEY_VALUE_DEFAULT);
            case KEY_VALUE -> settings.storageSettings().enableKeyValue(TestingStorage.KEY_VALUE_DEFAULT);
        }
        return Testing.testStartup(settings, SQL_DB::savepoint, SQL_DB.combinedTestingModule()).getInstance(SimpleUserManager.class);
    }

    private enum Scenario {
        SQL,
        KEY_VALUE
    }
}
