package io.webby.auth.user;

import io.webby.auth.BaseCoreIntegrationTest;
import io.webby.testing.TestingModels;
import io.webby.testing.ext.SqlDbExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@Tag("sql")
public class UserStoreIntegrationTest extends BaseCoreIntegrationTest {
    @RegisterExtension static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withManualCleanup(UserTable.META);

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void no_users(Scenario scenario) {
        UserStore users = startup(scenario, SQL.settings()).getInstance(UserStore.class);
        assertNull(users.getUserByIdOrNull(0));
        assertNull(users.getUserByIdOrNull(1));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void create_one_user(Scenario scenario) {
        UserStore users = startup(scenario, SQL.settings()).getInstance(UserStore.class);
        UserModel user = users.createUserAutoId(DefaultUser.newUserData(UserAccess.Simple));
        assertTrue(user.userId() > 0);
        assertEquals(user, users.getUserByIdOrNull(user.userId()));
        assertNull(users.getUserByIdOrNull(0));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void invalid_user_with_id(Scenario scenario) {
        UserStore users = startup(scenario, SQL.settings()).getInstance(UserStore.class);
        DefaultUser user = TestingModels.newUser(1, UserAccess.Simple);
        assertThrows(AssertionError.class, () -> users.createUserAutoId(user));
    }
}
