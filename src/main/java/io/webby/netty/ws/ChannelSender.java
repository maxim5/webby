package io.webby.netty.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.ws.lifecycle.AgentLifecycle;
import io.webby.ws.Sender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class ChannelSender implements AgentLifecycle, Sender {
    private final AtomicReference<State> state = new AtomicReference<>(State.NOT_INITIALIZED);
    private Channel channel;

    @Override
    public void onChannelConnected(@NotNull Channel channel) {
        this.channel = channel;
        boolean success = state.compareAndSet(State.NOT_INITIALIZED, State.READY);
        assert success : "Invalid state: %s".formatted(state.get());
    }

    @Override
    public void onChannelClose() {
        boolean success = state.compareAndSet(State.READY, State.CLOSED);
        assert success : "Invalid state: %s".formatted(state.get());
    }

    @Override
    public void onChannelRestored() {
        state.compareAndSet(State.CLOSED, State.READY);
    }

    @Override
    public @NotNull ChannelFuture send(@NotNull WebSocketFrame frame) {
        assert state.get() == State.READY :
            "Channel is not ready: %s. Reasons: Websocket handshake not completed or closed already".formatted(state);
        return channel.write(frame);
    }

    @Override
    public @NotNull ChannelOutboundInvoker flush() {
        assert state.get() == State.READY :
            "Channel is not ready: %s. Reasons: Websocket handshake not completed or closed already".formatted(state);
        return channel.flush();
    }

    @Override
    public @NotNull ChannelFuture sendFlush(@NotNull WebSocketFrame frame) {
        assert state.get() == State.READY :
            "Channel is not ready: %s. Reasons: Websocket handshake not completed or closed already".formatted(state);
        return channel.writeAndFlush(frame);
    }

    private enum State {
        NOT_INITIALIZED,
        READY,
        CLOSED
    }
}
