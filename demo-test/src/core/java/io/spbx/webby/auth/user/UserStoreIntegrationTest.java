package io.spbx.webby.auth.user;

import io.spbx.webby.auth.BaseCoreIntegrationTest;
import io.spbx.webby.testing.UserBuilder;
import io.spbx.webby.testing.ext.SqlDbExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("slow") @Tag("sql")
public class UserStoreIntegrationTest extends BaseCoreIntegrationTest {
    @RegisterExtension static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withManualCleanup(UserTable.META);

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void no_users(Scenario scenario) {
        UserStore users = startup(scenario, SQL.settings()).getInstance(UserStore.class);
        assertThat(users.getUserByIdOrNull(0)).isNull();
        assertThat(users.getUserByIdOrNull(1)).isNull();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void create_one_user(Scenario scenario) {
        UserStore users = startup(scenario, SQL.settings()).getInstance(UserStore.class);
        UserModel user = users.createUserAutoId(DefaultUser.newUserData(UserAccess.Simple));
        assertThat(user.userId() > 0).isTrue();
        assertThat(users.getUserByIdOrNull(user.userId())).isEqualTo(user);
        assertThat(users.getUserByIdOrNull(0)).isNull();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void invalid_user_with_id(Scenario scenario) {
        UserStore users = startup(scenario, SQL.settings()).getInstance(UserStore.class);
        DefaultUser user = UserBuilder.ofId(1).withAccess(UserAccess.Simple).build();
        assertThrows(AssertionError.class, () -> users.createUserAutoId(user));
    }
}
