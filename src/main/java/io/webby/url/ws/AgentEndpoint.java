package io.webby.url.ws;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AgentEndpoint {
    @NotNull Object instance();

    @Nullable Sender sender();

    @Nullable Object process(@NotNull WebSocketFrame message);
}
