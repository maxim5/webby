package io.webby.auth.user;

import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleUserManager implements BaseUserManager {
    private final UserStorage userStorage;

    @Inject
    public SimpleUserManager(@NotNull UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public @Nullable UserModel findByUserId(int userId) {
        return userStorage.findByUserId(userId);
    }

    public int createUserAutoId(@NotNull UserModel user) {
        assert user.isAutoId() : "User is not auto-id: %s".formatted(user);
        return userStorage.createUserAutoId(user);
    }
}
