package io.webby.auth.session;

import io.netty.handler.codec.http.HttpHeaders;
import io.webby.db.model.Ids;
import io.webby.netty.HttpConst;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public record Session(long sessionId, long userId, @NotNull Instant created, @NotNull String userAgent, @Nullable String ipAddress) {
    private static final long NO_USER_ID = Ids.FOREIGN_ENTITY_NOT_EXISTS_LONG;
    private static final long JUST_CREATED_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long TIME_TO_REFRESH_MILLIS = TimeUnit.DAYS.toMillis(30);

    public static @NotNull Session fromRequest(long sessionId, @NotNull HttpRequestEx request) {
        HttpHeaders headers = request.headers();
        String userAgent = headers.get(HttpConst.USER_AGENT, "");
        String ipAddress = request.remoteIPAddress();
        return new Session(sessionId, NO_USER_ID, Instant.now(), userAgent, ipAddress);
    }

    public boolean hasUser() {
        return userId != NO_USER_ID;
    }

    public boolean shouldRefresh() {
        long createdMillis = created.getEpochSecond() * 1000;
        long now = System.currentTimeMillis();
        return createdMillis + JUST_CREATED_MILLIS >= now || createdMillis + TIME_TO_REFRESH_MILLIS < now;
    }
}
