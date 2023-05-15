package io.webby.auth.session;

import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SessionStore {
    int size();

    @Nullable Session getSessionByIdOrNull(long sessionId);

    @NotNull Session createSessionAutoId(@NotNull HttpRequestEx request);

    void updateSessionById(@NotNull Session session);
}
