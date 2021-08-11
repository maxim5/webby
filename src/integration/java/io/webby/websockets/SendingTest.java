package io.webby.websockets;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Queue;

public class SendingTest extends BaseWebsocketIntegrationTest {
    private final Sending agent = testStartup(Sending.class);

    @Test
    public void text_simple() {
        Queue<WebSocketFrame> frames = sendText("foo");
    }
}
