package io.webby.auth.user;

import io.webby.testing.Mocking;
import io.webby.testing.TestingModels;
import org.junit.jupiter.api.Test;
import org.mockito.ScopedMock;

import java.time.Instant;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.db.model.IntAutoIdModel.AUTO_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultUserTest {
    private static final Instant INSTANT = Mocking.nowTruncatedToMillis();

    @Test
    public void newUserData_simple() {
        try (ScopedMock ignore = Mocking.withMockedInstantNow(INSTANT)) {
            UserData userData = DefaultUser.newUserData(UserAccess.Simple);
            assertThat(userData).isEqualTo(new DefaultUser(AUTO_ID, INSTANT, UserAccess.Simple));
        }
    }

    @Test
    public void toUserModel_success() {
        try (ScopedMock ignore = Mocking.withMockedInstantNow(INSTANT)) {
            UserData userData = DefaultUser.newUserData(UserAccess.Simple);
            UserModel model = userData.toUserModel(111);
            assertThat(model).isEqualTo(new DefaultUser(111, INSTANT, UserAccess.Simple));
        }
    }

    @Test
    public void toUserModel_fails() {
        UserData data = TestingModels.newUser(222);
        assertThrows(AssertionError.class, () -> data.toUserModel(333));
    }
}
