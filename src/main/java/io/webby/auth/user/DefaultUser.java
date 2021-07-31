package io.webby.auth.user;

import org.jetbrains.annotations.NotNull;

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
}
