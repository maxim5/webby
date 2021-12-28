package io.webby.auth.user;

import io.webby.orm.api.annotate.Model;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Objects;

@Model(exposeAs = UserModel.class)
public class DefaultUser implements UserModel {
    private int userId;
    private final Instant created;
    private final UserAccess access;

    public DefaultUser(int userId, @NotNull Instant created, @NotNull UserAccess access) {
        this.created = created;
        assert userId == AUTO_ID || userId > 0: "Invalid userId=%d".formatted(userId);
        this.userId = userId;
        this.access = access;
    }

    public static @NotNull DefaultUser newAuto(@NotNull UserAccess access) {
        return new DefaultUser(AUTO_ID, Instant.now(), access);
    }

    @Override
    public int userId() {
        return userId;
    }

    @Override
    public @NotNull Instant created() {
        return created;
    }

    @Override
    public @NotNull UserAccess access() {
        return access;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserModel that && userId == that.userId() &&
               created.equals(that.created()) && access.equals(that.access());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, created, access);
    }

    @Override
    public String toString() {
        return "DefaultUser{%d: access=%s}".formatted(userId, access);
    }

    public void resetIdToAuto() {
        userId = AUTO_ID;
    }

    public void setIfAutoIdOrDie(int newId) {
        assert userId == AUTO_ID : "Failed to set auto-inc id to user: %s".formatted(this);
        userId = newId;
    }
}
