package io.webby.websockets;

import com.google.common.truth.Truth;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.AssertFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.webby.testing.TestingBytes.asByteBuf;

public class HelloWebsocketTest extends BaseWebsocketIntegrationTest {
    private final HelloWebsocket agent = testStartup(HelloWebsocket.class);

    @Test
    public void text_simple() {
        Queue<WebSocketFrame> frames = sendText("foo");
        AssertFrame.assertFrames(frames, "Ack foo");
        Truth.assertThat(agent.getFrames()).containsExactly("foo");
    }

    @Test
    public void binary_simple() {
        Queue<WebSocketFrame> frames = sendBinary("bar");
        AssertFrame.assertFrames(frames);
        Truth.assertThat(agent.getFrames()).containsExactly(asByteBuf("bar"));
    }
}
