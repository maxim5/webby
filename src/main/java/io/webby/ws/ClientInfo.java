package io.webby.ws;

import io.webby.auth.session.Session;
import io.webby.auth.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ClientInfo(@NotNull Optional<String> version,
                         @NotNull Optional<ClientFrameType> preferredType,
                         @NotNull Optional<Session> session,
                         @NotNull Optional<User> user) {
    public @Nullable Session sessionOrNull() {
        return session.orElse(null);
    }

    public @NotNull Session sessionOrDie() {
        return session.orElseThrow();
    }

    public @Nullable User userOrNull() {
        return user.orElse(null);
    }

    public @NotNull User userOrDie() {
        return user.orElseThrow();
    }
}
