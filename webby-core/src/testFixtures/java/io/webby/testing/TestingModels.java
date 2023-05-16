package io.webby.testing;

import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

import static com.google.common.truth.Truth.assertThat;

public class TestingModels {
    public static @NotNull DefaultUser newUser(int userId) {
        return newUser(userId, UserAccess.Simple);
    }

    public static @NotNull DefaultUser newUser(int userId, @NotNull UserAccess access) {
        return DefaultUser.newUser(userId, Instant.now(), access);
    }

    public static @NotNull Session newSessionNow(long sessionId) {
        return newSession(sessionId, Instant.now());
    }

    public static @NotNull Session newSessionNowFixMillis(long sessionId) {
        return newSessionFixMillis(sessionId, Instant.now());
    }

    public static @NotNull Session newSessionNowWithoutIp(long sessionId) {
        return newSession(sessionId, Instant.now(), null);
    }

    public static @NotNull Session newSession(long sessionId, @NotNull Instant instant) {
        return newSession(sessionId, instant, "127.0.0.1");
    }

    public static @NotNull Session newSessionFixMillis(long sessionId, @NotNull Instant instant) {
        return newSession(sessionId, instant, "127.0.0.1");
    }

    public static @NotNull Session newSession(long sessionId, @NotNull Instant instant, @Nullable String ipAddress) {
        return new Session(sessionId, ForeignInt.empty(), instant, "User-Agent", ipAddress);
    }

    public static void assertSameSession(@Nullable Session actual, @Nullable Session expected) {
        assertThat(actual).isEqualTo(expected);
    }
}
