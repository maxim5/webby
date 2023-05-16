package io.webby.auth.session;

import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SessionStore {
    int size();

    @Nullable SessionModel getSessionByIdOrNull(long sessionId);

    @NotNull SessionModel createSessionAutoId(@NotNull HttpRequestEx request);

    void updateSessionById(@NotNull SessionModel session);
}
