package io.webby.auth.user;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.util.sql.api.ForeignLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserManager {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private final UserStorage userStorage;

    @Inject
    public UserManager(@NotNull UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public @Nullable User findByUserId(long userId) {
        return userStorage.findByUserId(userId);
    }

    public @Nullable User findByUserId(@NotNull ForeignLong<User> foreignId) {
        return foreignId.hasEntity() ? foreignId.getEntity() : findByUserId(foreignId.getLongId());
    }

    public long createUserAutoId(@NotNull User user) {
        assert user.isAutoId() : "User is not auto-id: %s".formatted(user);
        return userStorage.createUserAutoId(user);
    }
}
