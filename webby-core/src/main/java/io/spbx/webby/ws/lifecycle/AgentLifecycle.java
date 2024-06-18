package io.spbx.webby.ws.lifecycle;

import io.netty.channel.Channel;
import io.spbx.webby.ws.context.ClientInfo;
import org.jetbrains.annotations.NotNull;

public interface AgentLifecycle {
    default void onBeforeHandshake(@NotNull ClientInfo clientInfo) {}

    default void onChannelConnected(@NotNull Channel channel) {}

    default void onChannelClose() {}

    default void onChannelRestored() {}
}
