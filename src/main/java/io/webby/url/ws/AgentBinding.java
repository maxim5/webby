package io.webby.url.ws;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public record AgentBinding(@NotNull String url,
                           @NotNull Class<?> agentClass,
                           @NotNull Class<?> messageClass,
                           @NotNull Map<Class<?>, Method> acceptors,
                           @Nullable Field sender,
                           boolean acceptsFrame) {
}
