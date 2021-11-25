package io.webby.auth.user;

import io.webby.app.AppSettings;
import io.webby.db.kv.StorageType;
import io.webby.testing.Testing;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static io.webby.db.model.LongAutoIdModel.AUTO_ID;
import static io.webby.db.sql.SqlSettings.SQLITE_IN_MEMORY;
import static io.webby.db.sql.testing.TableHelper.CREATE_USER_TABLE_SQL;
import static org.junit.jupiter.api.Assertions.*;

public class UserManagerIntegrationTest {
    @RegisterExtension private final static SqlDbSetupExtension SQL_DB =
            new SqlDbSetupExtension(SQLITE_IN_MEMORY, CREATE_USER_TABLE_SQL);

    @AfterEach
    public void tearDown() {
        Testing.Internals.terminate();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void no_users(Scenario scenario) {
        UserManager userManager = startup(scenario);
        assertNull(userManager.findByUserId(0));
        assertNull(userManager.findByUserId(1));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void create_one_user(Scenario scenario) {
        UserManager userManager = startup(scenario);
        DefaultUser user = new DefaultUser(AUTO_ID, UserAccess.Simple);
        long userId = userManager.createUserAutoId(user);
        assertEquals(userId, user.userId());
        assertTrue(userId > 0);
        assertEquals(user, userManager.findByUserId(userId));
        assertNull(userManager.findByUserId(0));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void invalid_user_with_id(Scenario scenario) {
        UserManager userManager = startup(scenario);
        DefaultUser user = new DefaultUser(1, UserAccess.Simple);
        assertThrows(AssertionError.class, () -> userManager.createUserAutoId(user));
    }

    private static @NotNull UserManager startup(@NotNull Scenario scenario) {
        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(DefaultUser.class);
        switch (scenario) {
            case SQL -> settings.storageSettings().enableSqlStorage(SQL_DB.getSettings()).enableKeyValueStorage(StorageType.JAVA_MAP);
            case KEY_VALUE -> settings.storageSettings().enableKeyValueStorage(StorageType.JAVA_MAP);
        }
        return Testing.testStartup(settings, SQL_DB.singleConnectionPoolModule()).getInstance(UserManager.class);
    }

    private enum Scenario {
        SQL,
        KEY_VALUE
    }
}
