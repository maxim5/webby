package io.webby.testing;

import io.webby.auth.session.DefaultSession;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class TestingModels {
    public static @NotNull DefaultUser newUser(int userId) {
        return newUser(userId, UserAccess.Simple);
    }

    public static @NotNull DefaultUser newUser(int userId, @NotNull UserAccess access) {
        return DefaultUser.newUser(userId, Instant.now(), access);
    }

    public static @NotNull DefaultSession newSessionNow(long sessionId) {
        return newSession(sessionId, Instant.now());
    }

    public static @NotNull DefaultSession newSessionNowWithoutIp(long sessionId) {
        return newSession(sessionId, Instant.now(), null);
    }

    public static @NotNull DefaultSession newSession(long sessionId, @NotNull Instant instant) {
        return newSession(sessionId, instant, "127.0.0.1");
    }

    public static @NotNull DefaultSession newSession(long sessionId, @NotNull Instant instant, @Nullable String ipAddress) {
        return DefaultSession.newSession(sessionId, ForeignInt.empty(), instant, "User-Agent", ipAddress);
    }
}
