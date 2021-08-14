package io.webby.url.ws;

import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public record AgentBinding(@NotNull String url,
                           @NotNull Class<?> agentClass,
                           @NotNull Class<?> messageClass,
                           @NotNull FrameType frameType,
                           @NotNull Marshal marshal,
                           @NotNull List<Acceptor> acceptors,
                           @Nullable Field sender,
                           boolean acceptsFrame) {
}
