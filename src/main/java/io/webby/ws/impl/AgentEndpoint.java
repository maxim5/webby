package io.webby.ws.impl;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.ws.Constants.RequestIds;
import io.webby.ws.lifecycle.AgentLifecycle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AgentEndpoint {
    @NotNull Object instance();

    @NotNull AgentLifecycle lifecycle();

    void processIncoming(@NotNull WebSocketFrame frame, @NotNull Consumer consumer);

    @Nullable WebSocketFrame processOutgoing(long requestId, @NotNull Object message);

    @Nullable WebSocketFrame processError(long requestId, int code, @NotNull String message);

    interface Consumer {
        void accept(long requestId, @Nullable Object callResult);

        default void fail() {
            accept(RequestIds.NO_ID, null);
        }
    }
}
