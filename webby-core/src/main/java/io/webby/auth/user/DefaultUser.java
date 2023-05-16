package io.webby.auth.user;

import io.webby.orm.api.annotate.Model;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Model(exposeAs = UserModel.class, javaName = "User")
public class DefaultUser implements UserModel {
    protected int userId;
    protected final @NotNull Instant createdAt;
    protected final @NotNull UserAccess access;

    protected DefaultUser(int userId, @NotNull Instant createdAt, @NotNull UserAccess access) {
        assert userId == AUTO_ID || userId > 0: "Invalid userId=%d".formatted(userId);
        this.createdAt = createdAt;
        this.userId = userId;
        this.access = access;
    }

    public static @NotNull DefaultUser newUser(int userId, @NotNull Instant createdAt, @NotNull UserAccess access) {
        return new DefaultUser(userId, createdAt.truncatedTo(ChronoUnit.MILLIS), access);
    }

    public static @NotNull DefaultUser newAuto(@NotNull UserAccess access) {
        return newUser(AUTO_ID, Instant.now(), access);
    }

    @Override
    public int userId() {
        return userId;
    }

    @Override
    public @NotNull Instant createdAt() {
        return createdAt;
    }

    @Override
    public @NotNull UserAccess access() {
        return access;
    }

    @Override
    public void resetIdToAuto() {
        userId = AUTO_ID;
    }

    @Override
    public void setIfAutoIdOrDie(int newId) {
        assert userId == AUTO_ID : "Failed to set auto-inc id to user: %s".formatted(this);
        userId = newId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UserModel that &&
            userId == that.userId() &&
            createdAt.equals(that.createdAt()) &&
            access.equals(that.access());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, createdAt, access);
    }

    @Override
    public String toString() {
        return "DefaultUser{%d: access=%s}".formatted(userId, access);
    }
}
