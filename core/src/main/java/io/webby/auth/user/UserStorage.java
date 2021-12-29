package io.webby.auth.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserStorage {
    @Nullable UserModel findByUserId(int userId);

    int createUserAutoId(@NotNull UserModel user);
}
