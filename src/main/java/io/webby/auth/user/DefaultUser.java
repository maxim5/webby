package io.webby.auth.user;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DefaultUser implements User {
    private final long userId;
    private final UserAccess access;

    public DefaultUser(long userId, @NotNull UserAccess access) {
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
        return o instanceof User that && userId == that.userId() && Objects.equals(access, that.access());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, access);
    }
}
