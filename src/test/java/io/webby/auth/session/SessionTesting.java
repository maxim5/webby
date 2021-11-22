package io.webby.auth.session;

import io.webby.orm.api.ForeignLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class SessionTesting {
    public static Session newSessionNow() {
        return newSession(Instant.now());
    }

    public static Session newSessionWithoutIp() {
        return newSession(Instant.now(), null);
    }

    public static Session newSession(@NotNull Instant instant) {
        return newSession(instant, "127.0.0.1");
    }

    public static Session newSession(@NotNull Instant instant, @Nullable String ipAddress) {
        return new Session(123, ForeignLong.ofId(Session.NO_USER_ID), instant, "User-Agent", ipAddress);
    }
}
