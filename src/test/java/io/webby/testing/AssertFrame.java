package io.webby.testing;

import com.google.common.truth.Truth;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.ws.errors.ClientDeniedException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertFrame {
    public static void assertTextFrame(@NotNull WebSocketFrame frame, @NotNull String expected) {
        assertTextFrames(List.of(frame), expected);
    }

    public static void assertBinaryFrame(@NotNull WebSocketFrame frame, @NotNull String expected) {
        assertBinaryFrames(List.of(frame), expected);
    }

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
        assertFrames(frames, Arrays.stream(expected).map(TestingBytes::asByteBuf).map(BinaryWebSocketFrame::new).toList());
    }

    public static void assertFrames(@NotNull Iterable<WebSocketFrame> frames, @NotNull List<? extends WebSocketFrame> expected) {
        Truth.assertThat(frames).containsExactlyElementsIn(expected.stream().map(TestingBytes::replaceWithReadable).toList());
    }

    public static void assertNoFrames(@NotNull Iterable<WebSocketFrame> frames) {
        Truth.assertThat(frames).isEmpty();
    }

    public static void assertClientDenied(@NotNull Executable setup) {
        assertThrows(ClientDeniedException.class, setup);
    }
}
