package io.webby.auth.session;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public record Session(long sessionId, long userId, @NotNull Instant created, @NotNull String userAgent) {
    public static final long NO_USER_ID = -1;

    private static final long JUST_CREATED_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long TIME_TO_REFRESH_MILLIS = TimeUnit.DAYS.toMillis(30);

    public static Session fromRequest(long sessionId, @NotNull HttpRequestEx request) {
        HttpHeaders headers = request.headers();
        String userAgent = headers.get(HttpHeaderNames.USER_AGENT, "");
        return new Session(sessionId, NO_USER_ID, Instant.now(), userAgent);
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
