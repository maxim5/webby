package io.webby.auth.session;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

// TODO: IP address from channel
// https://stackoverflow.com/questions/15088253/netty-getremoteaddress-returns-different-port-every-time
// https://stackoverflow.com/questions/12292694/get-client-ip-address-from-jboss-netty-httprequest
public record Session(long sessionId, long userId, @NotNull Instant created, @NotNull String userAgent) {
    private static final long JUST_CREATED_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long TIME_TO_REFRESH_MILLIS = TimeUnit.DAYS.toMillis(30);

    public static Session fromRequest(long sessionId, @NotNull HttpRequest request) {
        HttpHeaders headers = request.headers();
        String userAgent = headers.get(HttpHeaderNames.USER_AGENT, "");
        return new Session(sessionId, -1, Instant.now(), userAgent);
    }

    public boolean shouldRefresh() {
        long createdMillis = created.getEpochSecond() * 1000;
        long now = System.currentTimeMillis();
        return createdMillis + JUST_CREATED_MILLIS >= now || createdMillis + TIME_TO_REFRESH_MILLIS < now;
    }
}
