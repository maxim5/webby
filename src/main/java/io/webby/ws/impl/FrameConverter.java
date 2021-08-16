package io.webby.ws.impl;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FrameConverter<M> {
    void toMessage(@NotNull WebSocketFrame frame, @NotNull Consumer<M> success, @NotNull Runnable failure);

    // true -> text, false -> binary, null -> unknown
    @Nullable Boolean peekFrameType(long requestId);

    @NotNull WebSocketFrame toFrame(long requestId, int code, @NotNull M message);

    interface Consumer<M> {
        void accept(@NotNull Acceptor acceptor, long requestId, @NotNull M payload);
    }
}
