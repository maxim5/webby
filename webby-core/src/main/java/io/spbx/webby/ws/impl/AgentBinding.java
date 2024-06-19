package io.spbx.webby.ws.impl;

import io.spbx.webby.url.annotate.FrameType;
import io.spbx.webby.url.annotate.Marshal;
import io.spbx.webby.ws.meta.FrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public record AgentBinding(@NotNull String url,
                           @NotNull Class<?> agentClass,
                           @NotNull Class<?> messageClass,
                           @NotNull FrameType frameType,
                           @NotNull Class<? extends FrameMetadata> metaClass,
                           @NotNull Marshal marshal,
                           @NotNull List<Acceptor> acceptors,
                           @Nullable Field senderField,
                           boolean acceptsFrame) {
}
