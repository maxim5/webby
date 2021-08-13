package io.webby.url.ws;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public record FrameConverterEndpoint(@NotNull Object instance,
                                     @NotNull FrameConverter<Object> converter,
                                     @Nullable Sender sender) implements AgentEndpoint {
    @Override
    public @Nullable Object process(@NotNull WebSocketFrame message) {
        AtomicReference<Object> callResult = new AtomicReference<>(null);
        converter.toMessage(message, (acceptor, requestId, payload) -> callResult.set(acceptor.call(instance, payload)), () -> {});
        return callResult.get();
    }
}
