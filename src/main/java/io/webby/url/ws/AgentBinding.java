package io.webby.url.ws;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record AgentBinding(@NotNull String url,
                           @NotNull Class<?> agentClass,
                           @NotNull Class<?> messageClass,
                           @NotNull Map<Class<?>, Method> acceptors,
                           boolean acceptsFrame) {
}
