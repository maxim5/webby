package io.webby.websockets;

import com.google.common.truth.Truth;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import io.webby.testing.Testing;
import io.webby.testing.TestingModules;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.url.ws.meta.*;
import io.webby.websockets.ExampleMessages.PrimitiveMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.webby.testing.FakeFrames.assertBinaryFrames;
import static io.webby.testing.FakeFrames.assertTextFrames;

public class AcceptJsonMessageTest extends BaseWebsocketIntegrationTest {
    @Test
    public void on_json_text_simple_only_int() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.TEXT, new TextSeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendText("primitive 777 {'i': 10}");
        assertTextFrames(frames, """
            777 0 {"i":10,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveInt(10));
    }

    @Test
    public void on_json_text_simple_only_long() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.TEXT, new TextSeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendText("primitive 12345678 {'l': -100}");
        assertTextFrames(frames, """
            12345678 0 {"i":0,"l":-100,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveLong(-100));
    }

    @Test
    public void on_json_text_default_request_id() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.TEXT, new TextSeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendText("primitive foo {'bool': true}");
        assertTextFrames(frames, """
            -1 0 {"i":0,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":true}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveBool(true));
    }

    @Test
    public void on_json_text_invalid_missing_request_id() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.TEXT, new TextSeparatorFrameMetadata());

        assertTextFrames(sendText("primitive {'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        assertTextFrames(sendText("primitive{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        assertTextFrames(sendText("primitive {}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_text_invalid_only_json() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.TEXT, new TextSeparatorFrameMetadata());

        assertTextFrames(sendText("{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        assertTextFrames(sendText("{}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_text_invalid_wrong_separation() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.TEXT, new TextSeparatorFrameMetadata());

        assertTextFrames(sendText("primitive123{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        assertTextFrames(sendText("primitive 123{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        assertTextFrames(sendText("primitive123 {'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        assertTextFrames(sendText("primitive  123 {'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        assertTextFrames(sendText("primitive\n123\n{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_text_acceptor_not_found() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.TEXT, new TextSeparatorFrameMetadata());
        assertTextFrames(sendText("foo 123 {'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_text_invalid_empty_payload() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.TEXT, new TextSeparatorFrameMetadata());
        assertTextFrames(sendText("primitive foo "));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_binary_simple_only_int() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.BINARY, new BinarySeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendBinary("primitive 12345678 {'i': 20}");
        assertBinaryFrames(frames, """
            12345678 \u0000 {"i":20,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveInt(20));
    }

    @Test
    public void on_json_binary_invalid_wrong_request_id() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.BINARY, new BinarySeparatorFrameMetadata());
        assertBinaryFrames(sendBinary("primitive 123 {'i': 20}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_binary_fixed() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.BINARY, new BinaryFixedSizeFrameMetadata(9));
        Queue<WebSocketFrame> frames = sendBinary("primitive12345678{'l':0}");
        assertBinaryFrames(frames, """
            12345678\u0000{"i":0,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveLong(0));
    }

    @Test
    public void on_json_binary_invalid() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.BINARY, new BinaryFixedSizeFrameMetadata(9));

        assertBinaryFrames(sendBinary("primitive 12345678{'i':0}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        assertBinaryFrames(sendBinary("primitive123{'i':0}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_metadata() {
        AcceptJsonMessage agent = prepareAgent(Marshal.JSON, FrameType.TEXT,
                                               new JsonMetadata(Testing.Internals.json, Testing.CHARSET));
        Queue<WebSocketFrame> frames = sendText("{'on': 'primitive', id: 123, data: \"{'ch': 'a'}\"}");
        assertTextFrames(frames, """
        {"id":123,"code":0,\
        "data":"{\\"i\\":0,\\"l\\":0,\\"b\\":0,\\"s\\":0,\\"ch\\":\\"a\\",\\"f\\":0.0,\\"d\\":0.0,\\"bool\\":false}"}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveChar('a'));
    }

    @NotNull
    private AcceptJsonMessage prepareAgent(@NotNull Marshal marshal,
                                           @NotNull FrameType frameType,
                                           @NotNull FrameMetadata metadata) {
        return testStartup(AcceptJsonMessage.class, settings -> {
            settings.setDefaultResponseContentMarshal(marshal);
            settings.setProperty("temp.ws.protocol", frameType.toString());
        }, TestingModules.instance(FrameMetadata.class, metadata));
    }

    @NotNull
    private static PrimitiveMessage primitiveInt(int i) {
        PrimitiveMessage message = new PrimitiveMessage();
        message.i = i;
        return message;
    }

    @NotNull
    private static PrimitiveMessage primitiveLong(long l) {
        PrimitiveMessage message = new PrimitiveMessage();
        message.l = l;
        return message;
    }

    @NotNull
    private static PrimitiveMessage primitiveChar(char ch) {
        PrimitiveMessage message = new PrimitiveMessage();
        message.ch = ch;
        return message;
    }

    @NotNull
    private static PrimitiveMessage primitiveBool(boolean bool) {
        PrimitiveMessage message = new PrimitiveMessage();
        message.bool = bool;
        return message;
    }
}
