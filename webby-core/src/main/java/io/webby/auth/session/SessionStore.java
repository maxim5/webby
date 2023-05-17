package io.webby.auth.session;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SessionStore {
    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    @Nullable SessionModel getSessionByIdOrNull(long sessionId);

    @NotNull SessionModel createSessionAutoId(@NotNull SessionData data);

    void updateSessionById(@NotNull SessionModel session);
}
