package io.webby.auth.user;

import org.jetbrains.annotations.NotNull;

public interface UserData {
    @NotNull UserModel toUserModel(int userId);
}
