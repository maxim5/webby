package io.webby.url.ws;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.ws.Constants.RequestIds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AgentEndpoint {
    @NotNull Object instance();

    @Nullable Sender sender();

    void processIncoming(@NotNull WebSocketFrame frame, @NotNull Consumer consumer);

    @Nullable WebSocketFrame processOutgoing(long requestId, @NotNull Object message);

    interface Consumer {
        void accept(long requestId, @Nullable Object callResult);

        default void fail() {
            accept(RequestIds.NO_ID, null);
        }
    }
}
