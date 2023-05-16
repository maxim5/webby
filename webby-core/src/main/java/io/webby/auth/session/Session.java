package io.webby.auth.session;

import io.netty.handler.codec.http.HttpHeaders;
import io.webby.auth.user.UserModel;
import io.webby.netty.HttpConst;
import io.webby.netty.request.HttpRequestEx;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.annotate.Model;
import io.webby.orm.api.annotate.Sql;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Model(exposeAs = SessionModel.class, javaName = "Session")
public class Session implements SessionModel {
    protected long sessionId;
    protected final @NotNull @Sql.Null ForeignInt<UserModel> user;
    protected final @NotNull Instant createdAt;
    protected final @NotNull String userAgent;
    protected final @Nullable @Sql.Null String ipAddress;

    protected Session(long sessionId,
                      @NotNull ForeignInt<UserModel> user,
                      @NotNull Instant createdAt,
                      @NotNull String userAgent,
                      @Nullable String ipAddress) {
        this.sessionId = sessionId;
        this.user = user;
        this.createdAt = createdAt;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    public static @NotNull Session newSession(long sessionId,
                                              @NotNull ForeignInt<UserModel> user,
                                              @NotNull Instant createdAt,
                                              @NotNull String userAgent,
                                              @Nullable String ipAddress) {
        return new Session(sessionId, user, createdAt.truncatedTo(ChronoUnit.MILLIS), userAgent, ipAddress);
    }

    public static @NotNull Session fromRequest(long sessionId, @NotNull HttpRequestEx request) {
        HttpHeaders headers = request.headers();
        String userAgent = headers.get(HttpConst.USER_AGENT, "");
        String ipAddress = request.remoteIPAddress();
        return newSession(sessionId, ForeignInt.empty(), Instant.now(), userAgent, ipAddress);
    }

    @Override
    public long sessionId() {
        return sessionId;
    }

    @Override
    public @NotNull ForeignInt<UserModel> user() {
        return user;
    }

    @Override
    public @NotNull Instant createdAt() {
        return createdAt;
    }

    @Override
    public @NotNull String userAgent() {
        return userAgent;
    }

    @Override
    public @Nullable String ipAddress() {
        return ipAddress;
    }

    public @NotNull Session withUser(@NotNull UserModel user) {
        return new Session(sessionId, ForeignInt.ofEntity(user.userId(), user), createdAt, userAgent, ipAddress);
    }

    public @NotNull Session withSessionId(long sessionId) {
        return new Session(sessionId, user, createdAt, userAgent, ipAddress);
    }

    @Override
    public void resetIdToAuto() {
        sessionId = AUTO_ID;
    }

    @Override
    public void setIfAutoIdOrDie(long newId) {
        assert sessionId == AUTO_ID : "Failed to set auto-inc id to session: %s".formatted(this);
        sessionId = newId;
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

    @Override
    public String toString() {
        return "Session[sessionId=%d, user=%s, createdAt=%s, userAgent=%s, ipAddress=%s]"
            .formatted(sessionId, user, createdAt, userAgent, ipAddress);
    }
}
