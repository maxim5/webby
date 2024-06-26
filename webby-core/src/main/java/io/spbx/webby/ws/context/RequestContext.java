package io.spbx.webby.ws.context;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.auth.session.SessionModel;
import io.spbx.webby.auth.user.UserModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RequestContext(long requestId,
                             @NotNull WebSocketFrame requestFrame,
                             @NotNull ClientInfo clientInfo) implements BaseRequestContext {
    public @Nullable SessionModel sessionOrNull() {
        return clientInfo.sessionOrNull();
    }

    public @NotNull SessionModel sessionOrDie() {
        return clientInfo.sessionOrDie();
    }

    public @Nullable UserModel userOrNull() {
        return clientInfo.userOrNull();
    }

    public @NotNull UserModel userOrDie() {
        return clientInfo.userOrDie();
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
