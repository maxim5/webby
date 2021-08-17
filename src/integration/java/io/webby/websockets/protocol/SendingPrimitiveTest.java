package io.webby.websockets.protocol;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.AssertFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import io.webby.testing.FakeClients;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.ws.ClientInfo;
import io.webby.ws.meta.FrameMetadata;
import io.webby.ws.meta.TextSeparatorFrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Queue;

public class SendingPrimitiveTest extends BaseWebsocketIntegrationTest {
    protected void setupJson(@NotNull FrameType type, @NotNull FrameMetadata metadata, @NotNull ClientInfo clientInfo) {
        setupAgent(SendingPrimitive.class, Marshal.JSON, type, metadata, clientInfo);
    }

    @Test
    public void on_json_text_no_context() {
        setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata(), FakeClients.DEFAULT);
        Queue<WebSocketFrame> frames = sendText("primitive 777 {'i': 10}");
        AssertFrame.assertTextFrames(frames, """
            -1 0 {"i":10,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":true}
        """.trim());
    }

    @Test
    public void on_json_text_with_context() {
        setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata(), FakeClients.DEFAULT);
        Queue<WebSocketFrame> frames = sendText("string 777 {'s': 'foo'}");
        AssertFrame.assertTextFrames(frames, """
            777 0 {"s":"Ack foo"}
        """.trim());
    }
}
