package io.webby.netty.ws;

import io.netty.channel.Channel;
import io.webby.url.ws.AgentLifecycle;
import org.jetbrains.annotations.NotNull;

public class AgentLifecycleAdapter implements AgentLifecycle {
    @Override
    public void onChannelConnected(@NotNull Channel channel) {
    }

    @Override
    public void onChannelClose() {
    }

    @Override
    public void onChannelRestored() {
    }
}
