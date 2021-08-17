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
    public @Nullable String versionOrNull() {
        return version.orElse(null);
    }

    public @NotNull String versionOrDie() {
        return version.orElseThrow();
    }

    public @Nullable ClientFrameType preferredTypeOrNull() {
        return preferredType.orElse(null);
    }

    public @NotNull ClientFrameType preferredTypeOrDie() {
        return preferredType.orElseThrow();
    }

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
