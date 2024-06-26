package io.spbx.webby.auth.session;

import com.google.errorprone.annotations.Immutable;
import io.netty.handler.codec.http.HttpHeaders;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.annotate.Model;
import io.spbx.orm.api.annotate.Sql;
import io.spbx.webby.auth.user.UserModel;
import io.spbx.webby.netty.HttpConst;
import io.spbx.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Model(exposeAs = SessionModel.class, javaName = "Session")
@Immutable
public class DefaultSession implements SessionModel {
    protected final long sessionId;
    protected final @NotNull @Sql.Null ForeignInt<UserModel> user;
    protected final @NotNull Instant createdAt;
    protected final @NotNull String userAgent;
    protected final @Nullable @Sql.Null String ipAddress;

    protected DefaultSession(long sessionId,
                             @NotNull ForeignInt<UserModel> user,
                             @NotNull Instant createdAt,
                             @NotNull String userAgent,
                             @Nullable String ipAddress) {
        assert user.isConsistent(UserModel::userId) : "Foreign user reference inconsistent: " + user;
        this.sessionId = sessionId;
        this.user = user;
        this.createdAt = createdAt;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    public static @NotNull DefaultSession newSession(long sessionId,
                                                     @NotNull ForeignInt<UserModel> user,
                                                     @NotNull Instant createdAt,
                                                     @NotNull String userAgent,
                                                     @Nullable String ipAddress) {
        return new DefaultSession(sessionId, user, createdAt.truncatedTo(ChronoUnit.MILLIS), userAgent, ipAddress);
    }

    public static @NotNull DefaultSession newSessionData(@NotNull HttpRequestEx request) {
        HttpHeaders headers = request.headers();
        String userAgent = headers.get(HttpConst.USER_AGENT, "");
        String ipAddress = request.remoteIPAddress();
        return newSession(AUTO_ID, ForeignInt.empty(), Instant.now(), userAgent, ipAddress);
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

    @Override
    public @NotNull DefaultSession withUser(@NotNull UserModel user) {
        assert !user.isAutoId() : "Incomplete user provided for the session: " + user;
        if (this.userId() == user.userId() && this.user.setEntityIfMissing(user)) {
            return this;
        }
        return new DefaultSession(sessionId, user.toForeignInt(), createdAt, userAgent, ipAddress);
    }

    @Override
    public @NotNull DefaultSession toSessionModel(long sessionId) {
        assert isAutoId() : "DefaultSession is not a pure data object: " + this;
        return new DefaultSession(sessionId, user, createdAt, userAgent, ipAddress);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultSession that &&
            sessionId == that.sessionId &&
            ForeignInt.isMatch(user, that.user) &&
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
