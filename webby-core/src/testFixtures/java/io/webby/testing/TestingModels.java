package io.webby.testing;

import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.auth.user.UserModel;
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
        return newSession(sessionId, instant.truncatedTo(ChronoUnit.MILLIS), "127.0.0.1");
    }

    public static @NotNull Session newSession(long sessionId, @NotNull Instant instant, @Nullable String ipAddress) {
        return new Session(sessionId, ForeignInt.ofId(UserModel.NO_USER_ID), instant, "User-Agent", ipAddress);
    }
}
