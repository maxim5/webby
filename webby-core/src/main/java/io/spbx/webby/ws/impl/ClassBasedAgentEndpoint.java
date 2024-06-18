package io.spbx.webby.ws.impl;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.netty.ws.FrameConst.RequestIds;
import io.spbx.webby.netty.ws.errors.BadFrameException;
import io.spbx.webby.netty.ws.sender.Sender;
import io.spbx.webby.ws.context.BaseRequestContext;
import io.spbx.webby.ws.context.ClientInfo;
import io.spbx.webby.ws.context.RequestContext;
import io.spbx.webby.ws.lifecycle.AgentLifecycle;
import io.spbx.webby.ws.lifecycle.AgentLifecycleFanOut;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record ClassBasedAgentEndpoint(@NotNull Object agent,
                                      @NotNull Map<Class<?>, Acceptor> acceptors,
                                      @Nullable Sender sender) implements AgentEndpoint {
    @Override
    public @NotNull AgentLifecycle lifecycle() {
        return AgentLifecycleFanOut.of(agent, sender);
    }

    public void processIncoming(@NotNull WebSocketFrame frame, @NotNull ClientInfo client, @NotNull CallResultConsumer consumer) {
        Class<?> klass = frame.getClass();
        Acceptor acceptor = acceptors.get(klass);
        if (acceptor != null) {
            RequestContext context = new RequestContext(RequestIds.NO_ID, frame, client);
            boolean forceRenderAsString = frame instanceof TextWebSocketFrame;
            Object callResult = acceptor.call(agent, frame, context, forceRenderAsString);
            consumer.accept(callResult, context);
        } else {
            throw new BadFrameException("Agent %s doesn't handle the frame: %s", agent, klass);
        }
    }

    @Override
    public @Nullable WebSocketFrame processOutgoing(@NotNull Object message, @NotNull RequestContext context) {
        return null;
    }

    @Override
    public @NotNull WebSocketFrame processError(int code, @NotNull String message, @NotNull BaseRequestContext context) {
        return new TextWebSocketFrame("%d: %s".formatted(code, message));
    }
}
