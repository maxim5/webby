package io.webby.testing;

import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class UserBuilder {
    protected final int userId;
    protected Instant createdAt = Instant.now();
    protected UserAccess access = UserAccess.Simple;

    protected UserBuilder(int userId) {
        this.userId = userId;
    }

    public static @NotNull DefaultUser simple(int userId) {
        return UserBuilder.ofId(userId).build();
    }

    public static @NotNull UserBuilder ofId(int userId) {
        assert userId > 0 : "Expected a positive userId: " + userId;
        return new UserBuilder(userId);
    }

    public static @NotNull UserBuilder ofAnyId(int userId) {
        return new UserBuilder(userId);
    }

    public @NotNull UserBuilder createdAt(@NotNull Instant instant) {
        this.createdAt = instant;
        return this;
    }

    public @NotNull UserBuilder withAccess(@NotNull UserAccess access) {
        this.access = access;
        return this;
    }

    public @NotNull DefaultUser build() {
        return DefaultUser.newUser(userId, createdAt, access);
    }
}
