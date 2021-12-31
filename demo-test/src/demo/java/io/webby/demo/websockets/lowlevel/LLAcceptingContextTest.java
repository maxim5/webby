package io.webby.demo.websockets.lowlevel;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import io.webby.testing.FakeClients;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.ws.context.ClientFrameType;
import io.webby.ws.context.ClientInfo;
import io.webby.ws.meta.FrameMetadata;
import io.webby.ws.meta.TextSeparatorFrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.webby.testing.AssertFrame.assertTextFrames;

public class LLAcceptingContextTest extends BaseWebsocketIntegrationTest {
    protected void setupJson(@NotNull FrameType type, @NotNull FrameMetadata metadata, @NotNull ClientInfo clientInfo) {
        setupAgent(LLAcceptingContext.class, Marshal.JSON, type, metadata, clientInfo);
    }

    @Test
    public void on_json_text_no_client() {
        setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata(), FakeClients.DEFAULT);
        Queue<WebSocketFrame> frames = sendText("foo");
        assertTextFrames(frames, "Ack foo null");
    }

    @Test
    public void on_json_text_with_client() {
        setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata(), FakeClients.client("2.0", ClientFrameType.ANY));
        Queue<WebSocketFrame> frames = sendText("bar");
        assertTextFrames(frames, "Ack bar 2.0");
    }
}
