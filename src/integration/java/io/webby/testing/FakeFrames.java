package io.webby.testing;

import com.google.common.truth.Truth;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class FakeFrames {
    public static void assertFrames(@NotNull Iterable<WebSocketFrame> frames) {
        Truth.assertThat(frames).isEmpty();
    }

    public static void assertFrames(@NotNull Iterable<WebSocketFrame> frames, @NotNull String ... expected) {
        assertFrames(frames, Arrays.stream(expected).map(TextWebSocketFrame::new).toList());
    }

    public static void assertFrames(@NotNull Iterable<WebSocketFrame> frames, byte @NotNull [] ... expected) {
        assertFrames(frames, Arrays.stream(expected).map(Unpooled::wrappedBuffer).map(BinaryWebSocketFrame::new).toList());
    }

    public static void assertTextFrames(@NotNull Iterable<WebSocketFrame> frames, @NotNull String ... expected) {
        assertFrames(frames, expected);
    }

    public static void assertBinaryFrames(@NotNull Iterable<WebSocketFrame> frames, @NotNull String ... expected) {
        assertFrames(frames, Arrays.stream(expected).map(s -> s.getBytes(Testing.CHARSET)).map(Unpooled::wrappedBuffer).map(BinaryWebSocketFrame::new).toList());
    }

    public static void assertFrames(@NotNull Iterable<WebSocketFrame> frames, @NotNull List<? extends WebSocketFrame> expected) {
        Truth.assertThat(frames).containsExactlyElementsIn(expected);
    }

    public static byte @NotNull [] bytes(@NotNull String str) {
        return str.getBytes(Testing.CHARSET);
    }

    public static @NotNull String toBytesString(@Nullable WebSocketFrame frame) {
        return frame != null ? Arrays.toString(frame.content().array()) : "<null>";
    }
}
