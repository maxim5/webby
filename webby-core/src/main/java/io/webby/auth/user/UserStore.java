package io.webby.auth.user;

import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserStore {
    int size();

    @NotNull Iterable<? extends UserModel> fetchAllUsers();

    @Nullable UserModel getUserByIdOrNull(int userId);

    default @Nullable UserModel getUserByIdOrNull(@NotNull ForeignInt<? extends UserModel> foreignId) {
        return foreignId.hasEntity() ? foreignId.getEntity() : getUserByIdOrNull(foreignId.getIntId());
    }

    int createUserAutoId(@NotNull UserModel user);
}
