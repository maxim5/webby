package io.webby.auth.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserStorage {
    @Nullable User findByUserId(long userId);

    long createUserAutoId(@NotNull User user);
}
