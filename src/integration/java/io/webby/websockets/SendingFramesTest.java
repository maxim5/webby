package io.webby.websockets;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.AssertFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Queue;

public class SendingFramesTest extends BaseWebsocketIntegrationTest {
    private final SendingFrames agent = testStartup(SendingFrames.class);

    @Test
    public void text_simple() {
        Queue<WebSocketFrame> frames = sendText("foo");
        AssertFrame.assertFrames(frames, "Ack foo");
    }

    @Test
    public void text_countdown_immediately() {
        agent.countDownImmediately(3);
        AssertFrame.assertFrames(readAllFramesUntilEmpty(), "3", "2", "1");
    }

    @Test
    public void text_countdown_listener() {
        agent.countDownViaListener(3);
        AssertFrame.assertFrames(readAllFramesUntilEmpty(), "3", "2", "1");
    }
}
