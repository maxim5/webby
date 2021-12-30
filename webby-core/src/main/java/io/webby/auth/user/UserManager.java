package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserManager {
    private final UserStorage userStorage;

    @Inject
    public UserManager(@NotNull UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public @Nullable UserModel findByUserId(int userId) {
        return userStorage.findByUserId(userId);
    }

    public @Nullable UserModel findByUserId(@NotNull ForeignInt<UserModel> foreignId) {
        return foreignId.hasEntity() ? foreignId.getEntity() : findByUserId(foreignId.getIntId());
    }

    public int createUserAutoId(@NotNull UserModel user) {
        assert user.isAutoId() : "User is not auto-id: %s".formatted(user);
        return userStorage.createUserAutoId(user);
    }
}
