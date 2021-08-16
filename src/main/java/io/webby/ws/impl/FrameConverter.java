package io.webby.ws.impl;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.ws.BaseRequestContext;
import io.webby.ws.RequestContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FrameConverter<M> {
    void toMessage(@NotNull WebSocketFrame frame, @NotNull ParsedFrameConsumer<M> success);

    // true -> text, false -> binary, null -> unknown
    @Nullable Boolean peekFrameType(@NotNull BaseRequestContext context);

    @NotNull WebSocketFrame toFrame(@NotNull BaseRequestContext context, int code, @NotNull M message);

    interface ParsedFrameConsumer<M> {
        void accept(@NotNull Acceptor acceptor, @NotNull RequestContext context, @NotNull M payload);
    }
}
