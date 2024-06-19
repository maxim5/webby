package io.spbx.webby.auth.session;

import org.jetbrains.annotations.NotNull;

public interface SessionData {
    @NotNull SessionModel toSessionModel(long sessionId);
}
