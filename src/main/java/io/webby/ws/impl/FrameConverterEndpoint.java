package io.webby.ws.impl;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.ws.Constants.StatusCodes;
import io.webby.ws.BaseRequestContext;
import io.webby.ws.ClientInfo;
import io.webby.ws.RequestContext;
import io.webby.ws.Sender;
import io.webby.ws.lifecycle.AgentLifecycle;
import io.webby.ws.lifecycle.AgentLifecycleFanOut;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FrameConverterEndpoint(@NotNull Object instance,
                                     @NotNull FrameConverter<Object> converter,
                                     @Nullable Sender sender) implements AgentEndpoint {
    @Override
    public @NotNull AgentLifecycle lifecycle() {
        return AgentLifecycleFanOut.of(instance, sender, converter);
    }

    @Override
    public void processIncoming(@NotNull WebSocketFrame frame, @NotNull ClientInfo client, @NotNull CallResultConsumer consumer) {
        converter.toMessage(frame, (acceptor, requestContext, payload) -> {
            boolean forceRenderAsString = converter().peekFrameType(requestContext) == Boolean.TRUE;
            Object callResult = acceptor.call(instance, payload, forceRenderAsString);
            consumer.accept(requestContext, callResult);
        });
    }

    @Override
    public @NotNull WebSocketFrame processOutgoing(@NotNull RequestContext context, @NotNull Object message) {
        return converter.toFrame(context, StatusCodes.OK, message);
    }

    @Override
    public @NotNull WebSocketFrame processError(@NotNull BaseRequestContext context, int code, @NotNull String message) {
        return converter.toFrame(context, code, message);
    }
}
