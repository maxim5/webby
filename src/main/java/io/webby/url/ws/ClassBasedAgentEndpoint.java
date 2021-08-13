package io.webby.url.ws;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record ClassBasedAgentEndpoint(@NotNull Object instance,
                                      @NotNull Map<Class<?>, Acceptor> acceptors,
                                      @Nullable Sender sender) implements AgentEndpoint {
    public @Nullable Object process(@NotNull WebSocketFrame frame) {
        Class<?> klass = frame.getClass();
        Acceptor acceptor = acceptors.get(klass);
        if (acceptor != null) {
            return acceptor.call(instance, frame);
        }
        return null;
    }
}
