package io.webby.testing;

import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TestingModels {
    public static @NotNull DefaultUser newUserNow(int userId) {
        return newUserNow(userId, UserAccess.Simple);
    }

    public static @NotNull DefaultUser newUserNow(int userId, @NotNull UserAccess access) {
        return newUser(userId, Instant.now(), access);
    }

    public static @NotNull DefaultUser newUser(int userId, @NotNull Instant createdAt) {
        return newUser(userId, createdAt, UserAccess.Simple);
    }

    public static @NotNull DefaultUser newUserNowFixMillis(int userId) {
        return newUserFixMillis(userId, Instant.now());
    }

    public static @NotNull DefaultUser newUserFixMillis(int userId, @NotNull Instant createdAt) {
        return newUser(userId, createdAt.truncatedTo(ChronoUnit.MILLIS), UserAccess.Simple);
    }

    public static @NotNull DefaultUser newUser(int userId, @NotNull Instant createdAt, @NotNull UserAccess access) {
        return new DefaultUser(userId, createdAt, access);
    }

    public static @NotNull Session newSessionNow() {
        return newSession(Instant.now());
    }

    public static @NotNull Session newSessionWithoutIp() {
        return newSession(Instant.now(), null);
    }

    public static @NotNull Session newSession(@NotNull Instant instant) {
        return newSession(instant, "127.0.0.1");
    }

    public static @NotNull Session newSession(@NotNull Instant instant, @Nullable String ipAddress) {
        return new Session(123, ForeignInt.ofId(Session.NO_USER_ID), instant, "User-Agent", ipAddress);
    }
}
