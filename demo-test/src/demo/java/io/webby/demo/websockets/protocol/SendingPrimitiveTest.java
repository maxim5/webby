package io.webby.demo.websockets.protocol;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.testing.BaseWebsocketIntegrationTest;
import io.spbx.webby.testing.FakeClients;
import io.spbx.webby.url.annotate.FrameType;
import io.spbx.webby.url.annotate.Marshal;
import io.spbx.webby.ws.context.ClientInfo;
import io.spbx.webby.ws.meta.TextSeparatorFrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.spbx.webby.testing.AssertFrame.assertTextFrames;

public class SendingPrimitiveTest extends BaseWebsocketIntegrationTest {
    protected void setupJson(@NotNull ClientInfo clientInfo) {
        setupAgent(SendingPrimitive.class, Marshal.JSON, FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata(), clientInfo);
    }

    @Test
    public void on_json_text_no_context() {
        setupJson(FakeClients.DEFAULT);
        Queue<WebSocketFrame> frames = sendText("primitive 777 {'i': 10}");
        assertTextFrames(frames, """
            -1 0 {"i":10,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":true}
        """.trim());
    }

    @Test
    public void on_json_text_with_context() {
        setupJson(FakeClients.DEFAULT);
        Queue<WebSocketFrame> frames = sendText("string 777 {'s': 'foo'}");
        assertTextFrames(frames, """
            777 0 {"s":"Ack foo"}
        """.trim());
    }
}
