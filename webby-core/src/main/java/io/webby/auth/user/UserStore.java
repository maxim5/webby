package io.webby.auth.user;

import io.webby.netty.errors.NotFoundException;
import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserStore {
    int size();

    @NotNull Iterable<? extends UserModel> fetchAllUsers();

    @Nullable UserModel getUserByIdOrNull(int userId);

    default @NotNull UserModel getUserByIdOr404(int userId) {
        UserModel user = getUserByIdOrNull(userId);
        NotFoundException.failIf(user == null, "User not found: id=%d", userId);
        return user;
    }

    default @Nullable UserModel getUserByIdOrNull(@NotNull ForeignInt<? extends UserModel> foreignId) {
        return foreignId.hasEntity() ? foreignId.getEntity() : getUserByIdOrNull(foreignId.getIntId());
    }

    default @NotNull UserModel getUserByIdOr404(@NotNull ForeignInt<? extends UserModel> foreignId) {
        UserModel user = getUserByIdOrNull(foreignId);
        NotFoundException.failIf(user == null, "User not found: foreign_id=%s", foreignId);
        return user;
    }

    int createUserAutoId(@NotNull UserModel user);
}
