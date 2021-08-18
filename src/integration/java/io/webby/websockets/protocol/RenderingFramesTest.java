package io.webby.websockets.protocol;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.AssertFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import io.webby.testing.FakeClients;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.ws.meta.TextSeparatorFrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Queue;

public class RenderingFramesTest extends BaseWebsocketIntegrationTest {
    protected void setupJson(@NotNull FrameType frameType) {
        setupAgent(RenderingFrames.class, Marshal.JSON, frameType, new TextSeparatorFrameMetadata(), FakeClients.DEFAULT);
    }

    @Test
    public void render_as_string() {
        setupJson(FrameType.TEXT_ONLY);
        Queue<WebSocketFrame> frames = sendText("str 777 {'s': '(foo)'}");
        AssertFrame.assertTextFrames(frames, """
            777 0 "\\u003cdiv\\u003e(foo)\\u003c/div\\u003e"
        """.trim());
    }

    @Test
    public void render_as_bytes() {
        setupJson(FrameType.BINARY_ONLY);
        Queue<WebSocketFrame> frames = sendBinary("str 777 {'s': 'XXX'}");
        AssertFrame.assertBinaryFrames(frames, """
            777 0 [60,100,105,118,62,88,88,88,60,47,100,105,118,62]
        """.trim());
    }
}
