package io.webby.demo.websockets.protocol;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import io.webby.testing.FakeClients;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.ws.context.ClientFrameType;
import io.webby.ws.context.ClientInfo;
import io.webby.ws.meta.TextSeparatorFrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.webby.testing.AssertFrame.assertTextFrames;

public class AcceptingContextTest extends BaseWebsocketIntegrationTest {
    protected void setupJson(@NotNull ClientInfo clientInfo) {
        setupAgent(AcceptingContext.class, Marshal.JSON, FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata(), clientInfo);
    }

    @Test
    public void on_json_text_no_client() {
        setupJson(FakeClients.DEFAULT);
        Queue<WebSocketFrame> frames = sendText("string 777 {'s': '*'}");
        assertTextFrames(frames, """
            777 0 {"s":"*-777-null"}
        """.trim());
    }

    @Test
    public void on_json_text_with_client() {
        setupJson(FakeClients.client("2.0", ClientFrameType.ANY));
        Queue<WebSocketFrame> frames = sendText("string 999 {'s': '$'}");
        assertTextFrames(frames, """
            999 0 {"s":"$-999-2.0"}
        """.trim());
    }
}
