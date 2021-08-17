package io.webby.ws.lifecycle;

import io.netty.channel.Channel;
import io.webby.ws.ClientInfo;
import org.jetbrains.annotations.NotNull;

public interface AgentLifecycle {
    default void onBeforeHandshake(@NotNull ClientInfo clientInfo) {}

    default void onChannelConnected(@NotNull Channel channel) {}

    default void onChannelClose() {}

    default void onChannelRestored() {}
}
