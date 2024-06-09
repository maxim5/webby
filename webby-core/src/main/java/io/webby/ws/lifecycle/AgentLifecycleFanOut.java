package io.webby.ws.lifecycle;

import io.netty.channel.Channel;
import io.webby.ws.context.ClientInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AgentLifecycleFanOut implements AgentLifecycle {
    private final List<AgentLifecycle> delegates;

    public AgentLifecycleFanOut(@NotNull List<AgentLifecycle> delegates) {
        this.delegates = delegates;
    }

    @Override
    public void onBeforeHandshake(@NotNull ClientInfo clientInfo) {
        delegates.forEach(delegate -> delegate.onBeforeHandshake(clientInfo));
    }

    @Override
    public void onChannelConnected(@NotNull Channel channel) {
        delegates.forEach(delegate -> delegate.onChannelConnected(channel));
    }

    @Override
    public void onChannelClose() {
        delegates.forEach(AgentLifecycle::onChannelClose);
    }

    @Override
    public void onChannelRestored() {
        delegates.forEach(AgentLifecycle::onChannelRestored);
    }

    @Override
    public String toString() {
        return "AgentLifecycleFanOut{delegates=%s}".formatted(delegates);
    }

    public static @NotNull AgentLifecycle of(@Nullable Object @NotNull ... listeners) {
        List<AgentLifecycle> delegates = Arrays.stream(listeners)
                .map(listener -> listener instanceof AgentLifecycle agentLifecycle ? agentLifecycle : null)
                .filter(Objects::nonNull)
                .toList();
        if (delegates.isEmpty()) {
            return new AgentLifecycleAdapter();
        } else if (delegates.size() == 1) {
            return delegates.getFirst();
        } else {
            return new AgentLifecycleFanOut(delegates);
        }
    }
}
