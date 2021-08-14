package io.webby.url.ws.lifecycle;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

public class AgentLifecycleAdapter implements AgentLifecycle {
    @Override
    public void onChannelConnected(@NotNull Channel channel) {
    }
}
