package io.webby.url.ws;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

public interface AgentLifecycle {
    void onChannelConnected(@NotNull Channel channel);

    void onChannelClose();

    void onChannelRestored();
}
