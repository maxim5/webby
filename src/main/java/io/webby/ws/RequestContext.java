package io.webby.ws;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jetbrains.annotations.NotNull;

public record RequestContext(long requestId,
                             @NotNull WebSocketFrame requestFrame,
                             @NotNull ClientInfo clientInfo) implements BaseRequestContext {
    @Override
    public boolean isTextRequest() {
        return requestFrame instanceof TextWebSocketFrame;
    }

    @Override
    public boolean isBinaryRequest() {
        return requestFrame instanceof BinaryWebSocketFrame;
    }
}
