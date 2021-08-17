package io.webby.ws.convert;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.ws.BaseRequestContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OutFrameConverter<M> {
    // true -> text, false -> binary, null -> unknown
    @Nullable Boolean peekFrameType(@NotNull BaseRequestContext context);

    @NotNull WebSocketFrame toFrame(@NotNull BaseRequestContext context, int code, @NotNull M message);
}
