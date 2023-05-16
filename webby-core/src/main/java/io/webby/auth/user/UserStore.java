package io.webby.auth.user;

import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.netty.errors.NotFoundException.getOrThrowNotFound;

public interface UserStore {
    int size();

    @NotNull Iterable<? extends UserModel> fetchAllUsers();

    @Nullable UserModel getUserByIdOrNull(int userId);

    default @NotNull UserModel getUserByIdOr404(int userId) {
        return getOrThrowNotFound(() -> getUserByIdOrNull(userId), "User not found: id=%d", userId);
    }

    default @Nullable UserModel getUserByIdOrNull(@NotNull ForeignInt<? extends UserModel> foreignId) {
        return foreignId.hasEntity() ? foreignId.getEntity() : getUserByIdOrNull(foreignId.getIntId());
    }

    default @NotNull UserModel getUserByIdOr404(@NotNull ForeignInt<? extends UserModel> foreignId) {
        return getOrThrowNotFound(() -> getUserByIdOrNull(foreignId), "User not found: foreign_id=%s", foreignId);
    }

    @NotNull UserModel createUserAutoId(@NotNull UserModel user);
}
