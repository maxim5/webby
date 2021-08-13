package io.webby.url.ws;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.ws.Constants.StatusCodes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FrameConverterEndpoint(@NotNull Object instance,
                                     @NotNull FrameConverter<Object> converter,
                                     @Nullable Sender sender) implements AgentEndpoint {
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
}
