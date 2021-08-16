package io.webby.ws.impl;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.ws.Constants.RequestIds;
import io.webby.netty.ws.errors.BadFrameException;
import io.webby.ws.BaseRequestContext;
import io.webby.ws.ClientInfo;
import io.webby.ws.RequestContext;
import io.webby.ws.Sender;
import io.webby.ws.lifecycle.AgentLifecycle;
import io.webby.ws.lifecycle.AgentLifecycleFanOut;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record ClassBasedAgentEndpoint(@NotNull Object instance,
                                      @NotNull Map<Class<?>, Acceptor> acceptors,
                                      @Nullable Sender sender) implements AgentEndpoint {
    @Override
    public @NotNull AgentLifecycle lifecycle() {
        return AgentLifecycleFanOut.of(instance, sender);
    }

    public void processIncoming(@NotNull WebSocketFrame frame, @NotNull ClientInfo client, @NotNull CallResultConsumer consumer) {
        Class<?> klass = frame.getClass();
        Acceptor acceptor = acceptors.get(klass);
        if (acceptor != null) {
            RequestContext context = new RequestContext(RequestIds.NO_ID, frame, client);
            boolean forceRenderAsString = frame instanceof TextWebSocketFrame;
            Object callResult = acceptor.call(instance, frame, forceRenderAsString);
            consumer.accept(context, callResult);
        } else {
            throw new BadFrameException("Agent %s doesn't handle the frame: %s".formatted(instance, klass));
        }
    }

    @Override
    public @Nullable WebSocketFrame processOutgoing(@NotNull RequestContext context, @NotNull Object message) {
        return null;
    }

    @Override
    public @NotNull WebSocketFrame processError(@NotNull BaseRequestContext context, int code, @NotNull String message) {
        return new TextWebSocketFrame("%d: %s".formatted(code, message));
    }
}
