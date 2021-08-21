package io.webby.examples.websockets.protocol;

import com.google.common.truth.Truth;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.*;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.ws.meta.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static io.webby.examples.websockets.protocol.ExampleMessages.PrimitiveMessage.*;

public class AcceptingPrimitiveTest extends BaseWebsocketIntegrationTest {
    protected @NotNull AcceptingPrimitive setupJson(@NotNull FrameType type, @NotNull FrameMetadata metadata) {
        return setupAgent(AcceptingPrimitive.class, Marshal.JSON, type, metadata, FakeClients.DEFAULT);
    }
    
    @Test
    public void on_json_text_simple_only_int() {
        AcceptingPrimitive agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendText("primitive 777 {'i': 10}");
        AssertFrame.assertTextFrames(frames, """
            777 0 {"i":10,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveInt(10));
    }

    @Test
    public void on_json_text_simple_only_long() {
        AcceptingPrimitive agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendText("primitive 12345678 {'l': -100}");
        AssertFrame.assertTextFrames(frames, """
            12345678 0 {"i":0,"l":-100,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveLong(-100));
    }

    @Test
    public void on_json_text_default_request_id() {
        AcceptingPrimitive agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendText("primitive foo {'bool': true}");
        AssertFrame.assertTextFrames(frames, """
            -1 0 {"i":0,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":true}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveBool(true));
    }

    @Test
    public void on_json_text_invalid_missing_request_id() {
        AcceptingPrimitive agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());

        AssertFrame.assertTextFrames(sendText("primitive {'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        AssertFrame.assertTextFrames(sendText("primitive{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        AssertFrame.assertTextFrames(sendText("primitive {}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_text_invalid_only_json() {
        AcceptingPrimitive agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());

        AssertFrame.assertTextFrames(sendText("{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        AssertFrame.assertTextFrames(sendText("{}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_text_invalid_wrong_separation() {
        AcceptingPrimitive agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());

        AssertFrame.assertTextFrames(sendText("primitive123{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        AssertFrame.assertTextFrames(sendText("primitive 123{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        AssertFrame.assertTextFrames(sendText("primitive123 {'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        AssertFrame.assertTextFrames(sendText("primitive  123 {'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        AssertFrame.assertTextFrames(sendText("primitive\n123\n{'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_text_acceptor_not_found() {
        AcceptingPrimitive agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());
        AssertFrame.assertTextFrames(sendText("foo 123 {'i': 10}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_text_invalid_empty_payload() {
        AcceptingPrimitive agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());
        AssertFrame.assertTextFrames(sendText("primitive foo "));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_binary_simple_only_int() {
        AcceptingPrimitive agent = setupJson(FrameType.BINARY_ONLY, new BinarySeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendBinary("primitive 12345678 {'i': 20}");
        AssertFrame.assertBinaryFrames(frames, """
            12345678 \u0000 {"i":20,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveInt(20));
    }

    @Test
    public void on_json_binary_invalid_wrong_request_id() {
        AcceptingPrimitive agent = setupJson(FrameType.BINARY_ONLY, new BinarySeparatorFrameMetadata());
        AssertFrame.assertBinaryFrames(sendBinary("primitive 123 {'i': 20}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_binary_fixed() {
        AcceptingPrimitive agent = setupJson(FrameType.BINARY_ONLY, new BinaryFixedSizeFrameMetadata(9));
        Queue<WebSocketFrame> frames = sendBinary("primitive12345678{'l':0}");
        AssertFrame.assertBinaryFrames(frames, """
            12345678\u0000{"i":0,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveLong(0));
    }

    @Test
    public void on_json_binary_invalid() {
        AcceptingPrimitive agent = setupJson(FrameType.BINARY_ONLY, new BinaryFixedSizeFrameMetadata(9));

        AssertFrame.assertBinaryFrames(sendBinary("primitive 12345678{'i':0}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();

        AssertFrame.assertBinaryFrames(sendBinary("primitive123{'i':0}"));
        Truth.assertThat(agent.getIncoming()).isEmpty();
    }

    @Test
    public void on_json_metadata() {
        AcceptingPrimitive agent = setupJson(FrameType.TEXT_ONLY,
                                             new JsonMetadata(Testing.Internals.json(), TestingBytes.CHARSET));
        Queue<WebSocketFrame> frames = sendText("{'on': 'primitive', id: 123, data: \"{'ch': 'a'}\"}");
        AssertFrame.assertTextFrames(frames, """
        {"id":123,"code":0,\
        "data":"{\\"i\\":0,\\"l\\":0,\\"b\\":0,\\"s\\":0,\\"ch\\":\\"a\\",\\"f\\":0.0,\\"d\\":0.0,\\"bool\\":false}"}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(primitiveChar('a'));
    }
}