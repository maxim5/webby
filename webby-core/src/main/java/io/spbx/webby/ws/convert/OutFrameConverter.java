package io.spbx.webby.ws.convert;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.ws.context.BaseRequestContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OutFrameConverter<M> {
    // true -> text, false -> binary, null -> unknown
    @Nullable Boolean peekFrameType(@NotNull BaseRequestContext context);

    @NotNull WebSocketFrame toFrame(int code, @NotNull M message, @NotNull BaseRequestContext context);
}
