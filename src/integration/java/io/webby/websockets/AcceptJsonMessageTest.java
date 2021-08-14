package io.webby.websockets;

import com.google.common.truth.Truth;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import io.webby.url.annotate.Marshal;
import io.webby.websockets.ExampleMessages.PrimitiveMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.webby.testing.FakeFrames.*;

public class AcceptJsonMessageTest extends BaseWebsocketIntegrationTest {
    @Test
    public void on_json_text() {
        AcceptJsonMessage agent = testStartup(AcceptJsonMessage.class, settings -> {
            settings.setDefaultResponseContentMarshal(Marshal.JSON);
            settings.setProperty("temp.ws.protocol", "TEXT");
        });

        Queue<WebSocketFrame> frames = sendText("primitive 777 {'i': 10}");

        assertTextFrames(frames, """
        777 0 {"i":10,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveInt(10));
    }

    @Test
    public void on_json_binary() {
        AcceptJsonMessage agent = testStartup(AcceptJsonMessage.class, settings -> {
            settings.setDefaultResponseContentMarshal(Marshal.JSON);
            settings.setProperty("temp.ws.protocol", "BINARY");
        });

        Queue<WebSocketFrame> frames = sendText("primitive 12345678 {'i': 20}");

        assertBinaryFrames(frames, """
        12345678 \u0000 {"i":20,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveInt(20));
    }

    @NotNull
    private static PrimitiveMessage primitiveInt(int i) {
        PrimitiveMessage message = new PrimitiveMessage();
        message.i = i;
        return message;
    }
}
