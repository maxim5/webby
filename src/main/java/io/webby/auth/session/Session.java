package io.webby.auth.session;

import io.netty.handler.codec.http.HttpHeaders;
import io.webby.db.model.Ids;
import io.webby.netty.HttpConst;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public record Session(long sessionId, long userId, @NotNull Instant created, @NotNull String userAgent, @Nullable String ipAddress) {
    public static final String DB_NAME = "session";

    private static final long NO_USER_ID = Ids.FOREIGN_ENTITY_NOT_EXISTS_LONG;

    public static @NotNull Session fromRequest(long sessionId, @NotNull HttpRequestEx request) {
        HttpHeaders headers = request.headers();
        String userAgent = headers.get(HttpConst.USER_AGENT, "");
        String ipAddress = request.remoteIPAddress();
        return new Session(sessionId, NO_USER_ID, Instant.now(), userAgent, ipAddress);
    }

    public boolean hasUser() {
        return userId != NO_USER_ID;
    }
}
