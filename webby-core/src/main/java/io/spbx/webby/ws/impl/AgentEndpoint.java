package io.spbx.webby.ws.impl;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.ws.context.BaseRequestContext;
import io.spbx.webby.ws.context.ClientInfo;
import io.spbx.webby.ws.context.RequestContext;
import io.spbx.webby.ws.lifecycle.AgentLifecycle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AgentEndpoint {
    @NotNull Object agent();

    @NotNull AgentLifecycle lifecycle();

    void processIncoming(@NotNull WebSocketFrame frame, @NotNull ClientInfo client, @NotNull CallResultConsumer consumer);

    @Nullable WebSocketFrame processOutgoing(@NotNull Object message, @NotNull RequestContext context);

    @Nullable WebSocketFrame processError(int code, @NotNull String message, @NotNull BaseRequestContext context);

    interface CallResultConsumer {
        void accept(@Nullable Object callResult, @NotNull RequestContext context);
    }
}
