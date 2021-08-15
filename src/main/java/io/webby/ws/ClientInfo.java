package io.webby.ws;

import io.webby.auth.session.Session;
import io.webby.auth.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ClientInfo(@NotNull Optional<String> version,
                         @NotNull Optional<ClientFrameType> preferredType,
                         @NotNull Optional<Session> session,
                         @NotNull Optional<User> user) {
}
