package io.spbx.webby.demo.websockets.lowlevel;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.testing.BaseWebsocketIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBytes.asByteBuf;
import static io.spbx.webby.testing.AssertFrame.assertFrames;

public class HelloWebsocketTest extends BaseWebsocketIntegrationTest {
    private final HelloWebsocket agent = testSetup(HelloWebsocket.class).initAgent();

    @Test
    public void text_simple() {
        Queue<WebSocketFrame> frames = sendText("foo");
        assertFrames(frames, "Ack foo");
        assertThat(agent.getFrames()).containsExactly("foo");
    }

    @Test
    public void binary_simple() {
        Queue<WebSocketFrame> frames = sendBinary("bar");
        assertFrames(frames);
        assertThat(agent.getFrames()).containsExactly(asByteBuf("bar"));
    }
}
