package io.webby.url.ws;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

public record AgentEndpoint(@NotNull Object instance,
                            @NotNull Map<Class<?>, Method> acceptors,
                            boolean acceptsFrame) {
    public @Nullable Object process(@NotNull Object message) throws Exception {
        Class<?> klass = message.getClass();
        Method method = acceptors.get(klass);
        if (method != null) {
            return method.invoke(instance, message);
        }
        // TODO: log?
        return null;
    }
}
