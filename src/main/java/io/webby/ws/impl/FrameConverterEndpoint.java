package io.webby.ws.impl;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.ws.Constants.StatusCodes;
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
    public void processIncoming(@NotNull WebSocketFrame frame, @NotNull Consumer consumer) {
        converter.toMessage(frame, (acceptor, requestId, payload) -> {
            Object callResult = acceptor.call(instance, payload);
            consumer.accept(requestId, callResult);
        }, consumer::fail);
    }

    @Override
    public @NotNull WebSocketFrame processOutgoing(long requestId, @NotNull Object message) {
        return converter.toFrame(requestId, StatusCodes.OK, message);
    }

    @Override
    public @NotNull WebSocketFrame processError(long requestId, int code, @NotNull String message) {
        return converter.toFrame(requestId, code, message);
    }
}
