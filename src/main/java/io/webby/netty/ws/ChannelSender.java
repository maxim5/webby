package io.webby.netty.ws;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.url.ws.AgentLifecycle;
import io.webby.url.ws.Sender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class ChannelSender implements AgentLifecycle, Sender {
    private final AtomicReference<State> state = new AtomicReference<>(State.NOT_INITIALIZED);
    private Channel channel;

    @Override
    public void accept(WebSocketFrame frame) {
        assert state.get() == State.READY :
            "Channel is not ready: %s. Reasons: Websocket handshake not completed or closed already".formatted(state.get());
        channel.write(frame);
    }

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

    private enum State {
        NOT_INITIALIZED,
        READY,
        CLOSED
    }
}
