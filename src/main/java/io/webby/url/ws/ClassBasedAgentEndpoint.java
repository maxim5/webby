package io.webby.url.ws;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.ws.Constants.RequestIds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record ClassBasedAgentEndpoint(@NotNull Object instance,
                                      @NotNull Map<Class<?>, Acceptor> acceptors,
                                      @Nullable Sender sender) implements AgentEndpoint {
    public void processIncoming(@NotNull WebSocketFrame frame, @NotNull Consumer consumer) {
        Class<?> klass = frame.getClass();
        Acceptor acceptor = acceptors.get(klass);
        if (acceptor != null) {
            consumer.accept(RequestIds.NO_ID, acceptor.call(instance, frame));
        } else {
            consumer.fail();
        }
    }

    @Override
    public @Nullable WebSocketFrame processOutgoing(long requestId, @NotNull Object message) {
        return null;
    }
}
