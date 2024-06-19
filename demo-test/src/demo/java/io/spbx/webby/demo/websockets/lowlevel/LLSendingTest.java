package io.spbx.webby.demo.websockets.lowlevel;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.testing.BaseWebsocketIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.spbx.webby.testing.AssertFrame.assertFrames;

public class LLSendingTest extends BaseWebsocketIntegrationTest {
    private final LLSending agent = testSetup(LLSending.class).initAgent();

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
