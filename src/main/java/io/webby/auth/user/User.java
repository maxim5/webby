package io.webby.auth.user;

import org.jetbrains.annotations.NotNull;

public interface User {
    long userId();

    @NotNull UserAccess access();
}
