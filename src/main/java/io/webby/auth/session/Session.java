package io.webby.auth.session;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record Session(long sessionId, @NotNull Instant created) {
    public boolean shouldRefresh() {
        return created.plusSeconds(10).isAfter(Instant.now());  // TODO: hacky
    }
}