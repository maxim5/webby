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
import java.time.temporal.ChronoUnit;
import java.util.Objects;

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
        return new Session(sessionId, ForeignInt.empty(), Instant.now().truncatedTo(ChronoUnit.MILLIS), userAgent, ipAddress);
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

    public @NotNull Session withSessionId(long sessionId) {
        return new Session(sessionId, user, createdAt, userAgent, ipAddress);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Session that &&
            sessionId == that.sessionId &&
            createdAt.equals(that.createdAt) &&
            userAgent.equals(that.userAgent) &&
            Objects.equals(ipAddress, that.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, user.getFk(), createdAt, userAgent, ipAddress);
    }
}
