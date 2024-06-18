package io.webby.testing;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.webby.netty.ws.sender.Sender;
import org.jetbrains.annotations.NotNull;

import static io.spbx.util.testing.TestingBytes.asBytes;

public class FakeFrames {
    public static @NotNull TextWebSocketFrame text(@NotNull String data) {
        return Sender.text(data);
    }

    public static @NotNull BinaryWebSocketFrame binary(@NotNull String data) {
        return Sender.binary(asBytes(data));
    }
}
