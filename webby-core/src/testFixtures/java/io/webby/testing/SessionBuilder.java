package io.webby.testing;

import io.webby.auth.session.DefaultSession;
import io.webby.auth.user.UserModel;
import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class SessionBuilder {
    public static final String IP_ADDRESS = "127.0.0.1";
    public static final String USER_AGENT = "User-Agent";

    protected final long sessionId;
    protected ForeignInt<UserModel> user = ForeignInt.empty();
    protected Instant createdAt = Instant.now();
    protected String userAgent = USER_AGENT;
    protected String ipAddress = IP_ADDRESS;

    protected SessionBuilder(long sessionId) {
        this.sessionId = sessionId;
    }

    public static @NotNull DefaultSession simple(long sessionId) {
        return SessionBuilder.ofId(sessionId).build();
    }

    public static @NotNull SessionBuilder ofId(long sessionId) {
        assert sessionId > 0 : "Expected a positive sessionId: " + sessionId;
        return new SessionBuilder(sessionId);
    }

    public static @NotNull SessionBuilder ofAnyId(int userId) {
        return new SessionBuilder(userId);
    }

    public @NotNull SessionBuilder withUserId(int userId) {
        assert userId > 0 : "Expected a positive userId: " + userId;
        this.user = ForeignInt.ofId(userId);
        return this;
    }

    public @NotNull SessionBuilder withUser(@NotNull UserModel user) {
        assert !user.isAutoId() : "Expected a concrete user: " + user;
        this.user = user.toForeignInt();
        return this;
    }

    public @NotNull SessionBuilder withUser(@NotNull UserBuilder userBuilder) {
        return withUser(userBuilder.build());
    }

    public @NotNull SessionBuilder createdAt(@NotNull Instant instant) {
        this.createdAt = instant;
        return this;
    }

    public @NotNull SessionBuilder withUserAgent(@NotNull String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public @NotNull SessionBuilder withIpAddress(@NotNull String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public @NotNull SessionBuilder withoutIpAddress() {
        this.ipAddress = null;
        return this;
    }

    public @NotNull DefaultSession build() {
        return DefaultSession.newSession(sessionId, user, createdAt, userAgent, ipAddress);
    }
}
