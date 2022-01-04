package io.webby.auth.user;

import org.jetbrains.annotations.NotNull;

public interface UserStore extends UserReadStore {
    int createUserAutoId(@NotNull UserModel user);
}
