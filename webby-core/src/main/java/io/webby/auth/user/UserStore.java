package io.webby.auth.user;

import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserStore {
    int size();

    @NotNull Iterable<? extends UserModel> fetchAllUsers();

    @Nullable UserModel findByUserId(int userId);

    default @Nullable UserModel findByUserId(@NotNull ForeignInt<? extends UserModel> foreignId) {
        return foreignId.hasEntity() ? foreignId.getEntity() : findByUserId(foreignId.getIntId());
    }

    int createUserAutoId(@NotNull UserModel user);
}
