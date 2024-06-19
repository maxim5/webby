package io.spbx.webby.ws.context;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.netty.ws.FrameConst.RequestIds;
import org.jetbrains.annotations.NotNull;

public record ErrorRequestContext(boolean isTextRequest) implements BaseRequestContext {
    public static final ErrorRequestContext DEFAULT = new ErrorRequestContext(true);

    public static @NotNull ErrorRequestContext of(@NotNull WebSocketFrame frame) {
        return new ErrorRequestContext(frame instanceof TextWebSocketFrame);
    }

    @Override
    public long requestId() {
        return RequestIds.NO_ID;
    }
}
