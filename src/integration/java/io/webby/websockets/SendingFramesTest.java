package io.webby.websockets;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.webby.testing.FakeFrames.assertFrames;

public class SendingFramesTest extends BaseWebsocketIntegrationTest {
    private final SendingFrames agent = testStartup(SendingFrames.class);

    @Test
    public void text_simple() {
        Queue<WebSocketFrame> frames = sendText("foo");
        assertFrames(frames, "Ack foo");
    }

    @Test
    public void text_countdown_immediately() {
        agent.countDownImmediately(3);
        assertFrames(readAllFramesUntilEmpty(), "3", "2", "1");
    }

    @Test
    public void text_countdown_listener() {
        agent.countDownViaListener(3);
        assertFrames(readAllFramesUntilEmpty(), "3", "2", "1");
    }
}
