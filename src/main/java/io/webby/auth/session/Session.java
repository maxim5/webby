package io.webby.auth.session;

import io.netty.handler.codec.http.HttpHeaders;
import io.webby.auth.user.User;
import io.webby.db.model.Ids;
import io.webby.netty.HttpConst;
import io.webby.netty.request.HttpRequestEx;
import io.webby.util.sql.api.ForeignLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public record Session(long sessionId,
                      @NotNull ForeignLong<User> user,
                      @NotNull Instant created,
                      @NotNull String userAgent,
                      @Nullable String ipAddress) {
    public static final String DB_NAME = "session";
    public static final long NO_USER_ID = Ids.FOREIGN_ENTITY_NOT_EXISTS_LONG;

    public static @NotNull Session fromRequest(long sessionId, @NotNull HttpRequestEx request) {
        HttpHeaders headers = request.headers();
        String userAgent = headers.get(HttpConst.USER_AGENT, "");
        String ipAddress = request.remoteIPAddress();
        return new Session(sessionId, ForeignLong.ofId(NO_USER_ID), Instant.now(), userAgent, ipAddress);
    }

    public long userId() {
        return user.getLongId();
    }

    public boolean hasUser() {
        return userId() != NO_USER_ID;
    }

    public @NotNull Session withUser(@NotNull User user) {
        return new Session(sessionId, ForeignLong.ofEntity(user.userId(), user), created, userAgent, ipAddress);
    }
}
