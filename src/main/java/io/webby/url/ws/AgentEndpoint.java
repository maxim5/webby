package io.webby.url.ws;

import com.google.common.flogger.FluentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;

public record AgentEndpoint(@NotNull Object instance,
                            @NotNull Map<Class<?>, Method> acceptors,
                            boolean acceptsFrame) {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public @Nullable Object process(@NotNull Object message) throws Exception {
        Class<?> klass = message.getClass();
        Method method = acceptors.get(klass);
        if (method != null) {
            Object callResult = method.invoke(instance, message);
            if (callResult == null && !isVoid(method)) {
                log.at(Level.WARNING).log("Websocket agent returned null: %s", method);
            }
            return callResult;
        }

        log.at(Level.INFO).log("Websocket agent doesn't handle the message: %s", klass);
        return null;
    }

    private static boolean isVoid(@NotNull Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }
}
