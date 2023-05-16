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

import static io.webby.db.model.IntAutoIdModel.AUTO_ID;
import static org.junit.jupiter.api.Assertions.*;

@Tag("sql")
public class UserStoreIntegrationTest {
    @RegisterExtension static final SqlDbSetupExtension SQL = SqlDbSetupExtension.fromProperties();

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void no_users(Scenario scenario) {
        UserStore users = startup(scenario);
        assertNull(users.getUserByIdOrNull(0));
        assertNull(users.getUserByIdOrNull(1));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void create_one_user(Scenario scenario) {
        UserStore users = startup(scenario);
        DefaultUser user = TestingModels.newUser(AUTO_ID);
        int userId = users.createUserAutoId(user);
        assertEquals(userId, user.userId());
        assertTrue(userId > 0);
        assertEquals(user, users.getUserByIdOrNull(userId));
        assertNull(users.getUserByIdOrNull(0));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void invalid_user_with_id(Scenario scenario) {
        UserStore users = startup(scenario);
        DefaultUser user = TestingModels.newUser(1, UserAccess.Simple);
        assertThrows(AssertionError.class, () -> users.createUserAutoId(user));
    }

    private static @NotNull UserStore startup(@NotNull Scenario scenario) {
        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(Testing.CORE_MODELS);
        switch (scenario) {
            case SQL -> settings.storageSettings().enableSql(SQL.settings()).enableKeyValue(TestingStorage.KEY_VALUE_DEFAULT);
            case KEY_VALUE -> settings.storageSettings().enableKeyValue(TestingStorage.KEY_VALUE_DEFAULT);
        }
        return Testing.testStartup(settings, SQL::savepoint, SQL.combinedTestingModule()).getInstance(UserStore.class);
    }

    private enum Scenario {
        SQL,
        KEY_VALUE,
    }
}
