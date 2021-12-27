package io.webby.auth.session;

import io.netty.handler.codec.http.HttpHeaders;
import io.webby.auth.user.User;
import io.webby.db.model.Ids;
import io.webby.netty.HttpConst;
import io.webby.netty.request.HttpRequestEx;
import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public record Session(long sessionId,
                      @NotNull ForeignInt<User> user,
                      @NotNull Instant created,
                      @NotNull String userAgent,
                      @Nullable String ipAddress) {
    public static final String DB_NAME = "session";
    public static final int NO_USER_ID = Ids.FOREIGN_ENTITY_NOT_EXISTS_INT;

    public static @NotNull Session fromRequest(long sessionId, @NotNull HttpRequestEx request) {
        HttpHeaders headers = request.headers();
        String userAgent = headers.get(HttpConst.USER_AGENT, "");
        String ipAddress = request.remoteIPAddress();
        return new Session(sessionId, ForeignInt.ofId(NO_USER_ID), Instant.now(), userAgent, ipAddress);
    }

    public int userId() {
        return user.getIntId();
    }

    public boolean hasUser() {
        return userId() != NO_USER_ID;
    }

    public @NotNull Session withUser(@NotNull User user) {
        return new Session(sessionId, ForeignInt.ofEntity(user.userId(), user), created, userAgent, ipAddress);
    }
}
