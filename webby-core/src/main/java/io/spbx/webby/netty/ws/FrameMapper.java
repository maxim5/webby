package io.spbx.webby.netty.ws;

import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.app.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FrameMapper {
    @Inject private Settings settings;

    public @Nullable WebSocketFrame mapInstance(@NotNull Object instance) {
        if (instance instanceof WebSocketFrame frame) {
            return frame;
        }

        if (instance instanceof String text) {
            return new TextWebSocketFrame(text);
        }
        if (instance instanceof CharSequence text) {
            return new TextWebSocketFrame(Unpooled.copiedBuffer(text, settings.charset()));
        }
        if (instance instanceof char[] array) {
            return new TextWebSocketFrame(Unpooled.copiedBuffer(array, settings.charset()));
        }

        if (instance instanceof byte[] bytes) {
            return new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes));
        }
        if (instance instanceof ByteBuf buf) {
            return new BinaryWebSocketFrame(buf);
        }

        return null;
    }
}
