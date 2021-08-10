package io.webby.url.ws;

import com.google.common.flogger.FluentLogger;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;

public record AgentEndpoint(@NotNull Object instance,
                            @NotNull Map<Class<?>, Method> acceptors,
                            boolean acceptsFrame) {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public @Nullable WebSocketFrame process(@NotNull WebSocketFrame message) throws Exception {
        Class<?> klass = message.getClass();
        Method method = acceptors.get(klass);
        if (method != null) {
            Object callResult = method.invoke(instance, message);
            if (callResult == null && !isVoid(method)) {
                log.at(Level.WARNING).log("Websocket agent returned null: %s", method);
            }
            if (callResult instanceof WebSocketFrame frame) {
                return frame;
            } else {
                log.at(Level.WARNING).log("Websocket agent returned unexpected object: %s", callResult);
            }
        }

        log.at(Level.INFO).log("Websocket agent doesn't handle the message: %s", klass);
        return null;
    }

    private static boolean isVoid(@NotNull Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }
}
