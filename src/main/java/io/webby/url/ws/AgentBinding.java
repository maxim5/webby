package io.webby.url.ws;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public record AgentBinding(@NotNull String url,
                           @NotNull Class<?> agentClass,
                           @NotNull Class<?> messageClass,
                           @NotNull List<Acceptor> acceptors,
                           @Nullable Field sender,
                           boolean acceptsFrame) {
}
