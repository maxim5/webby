package io.webby.url.ws.lifecycle;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

public interface AgentLifecycle {
    void onChannelConnected(@NotNull Channel channel);

    default void onChannelClose() {}

    default void onChannelRestored() {}
}
