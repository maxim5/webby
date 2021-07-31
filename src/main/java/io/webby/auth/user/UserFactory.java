package io.webby.auth.user;

import org.jetbrains.annotations.NotNull;

public interface UserFactory {
    @NotNull Class<? extends User> getUserClass();

    @NotNull User createNewUser();
}
