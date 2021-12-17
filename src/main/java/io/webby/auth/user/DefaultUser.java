package io.webby.auth.user;

import io.webby.orm.api.annotate.Model;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model(exposeAs = User.class)
public class DefaultUser implements User {
    private long userId;
    private final UserAccess access;

    public DefaultUser(long userId, @NotNull UserAccess access) {
        assert userId == AUTO_ID || userId > 0: "Invalid userId=%d".formatted(userId);
        this.userId = userId;
        this.access = access;
    }

    @Override
    public long userId() {
        return userId;
    }

    @Override
    public @NotNull UserAccess access() {
        return access;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User that && userId == that.userId() && access.equals(that.access());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, access);
    }

    @Override
    public String toString() {
        return "DefaultUser{%d: access=%s}".formatted(userId, access);
    }

    public void resetIdToAuto() {
        userId = AUTO_ID;
    }

    public void setIfAutoIdOrDie(long newId) {
        assert userId == AUTO_ID : "Failed to set auto-inc id to user: %s".formatted(this);
        userId = newId;
    }
}
