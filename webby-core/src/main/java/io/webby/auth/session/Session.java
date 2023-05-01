package io.webby.auth.session;

import io.netty.handler.codec.http.HttpHeaders;
import io.webby.auth.user.UserModel;
import io.webby.netty.HttpConst;
import io.webby.netty.request.HttpRequestEx;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.annotate.Sql;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public record Session(long sessionId,
                      @NotNull @Sql.Null ForeignInt<UserModel> user,
                      @NotNull Instant createdAt,
                      @NotNull String userAgent,
                      @Nullable @Sql.Null String ipAddress) {
    public static final String DB_NAME = "session";

    public static @NotNull Session fromRequest(long sessionId, @NotNull HttpRequestEx request) {
        HttpHeaders headers = request.headers();
        String userAgent = headers.get(HttpConst.USER_AGENT, "");
        String ipAddress = request.remoteIPAddress();
        return new Session(sessionId, ForeignInt.empty(), Instant.now(), userAgent, ipAddress);
    }

    public int userId() {
        return user.getIntId();
    }

    public boolean hasUser() {
        return user.isPresent();
    }

    public @NotNull Session withUser(@NotNull UserModel user) {
        return new Session(sessionId, ForeignInt.ofEntity(user.userId(), user), createdAt, userAgent, ipAddress);
    }
}
