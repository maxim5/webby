package io.webby.ws;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.auth.session.Session;
import io.webby.auth.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RequestContext(long requestId,
                             @NotNull WebSocketFrame requestFrame,
                             @NotNull ClientInfo clientInfo) implements BaseRequestContext {
    public @Nullable Session sessionOrNull() {
        return clientInfo.session().orElse(null);
    }

    public @NotNull Session sessionOrDie() {
        return clientInfo.session().orElseThrow();
    }

    public @Nullable User userOrNull() {
        return clientInfo.user().orElse(null);
    }

    public @NotNull User userOrDie() {
        return clientInfo.user().orElseThrow();
    }

    @Override
    public boolean isTextRequest() {
        return requestFrame instanceof TextWebSocketFrame;
    }

    @Override
    public boolean isBinaryRequest() {
        return requestFrame instanceof BinaryWebSocketFrame;
    }
}
