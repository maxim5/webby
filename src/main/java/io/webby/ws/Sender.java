package io.webby.ws;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jetbrains.annotations.NotNull;

public interface Sender {
    @CanIgnoreReturnValue
    @NotNull ChannelFuture send(@NotNull WebSocketFrame frame);

    @CanIgnoreReturnValue
    @NotNull ChannelOutboundInvoker flush();

    @CanIgnoreReturnValue
    @NotNull ChannelFuture sendFlush(@NotNull WebSocketFrame frame);

    static @NotNull WebSocketFrame text(@NotNull String text) {
        return new TextWebSocketFrame(text);
    }

    static @NotNull BinaryWebSocketFrame binary(byte @NotNull [] bytes) {
        return new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes));
    }
}
