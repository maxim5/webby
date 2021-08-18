package io.webby.websockets.protocol;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import io.webby.testing.FakeClients;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.websockets.protocol.ExampleMessages.PrimitiveMessage;
import io.webby.websockets.protocol.ExampleMessages.StringMessage;
import io.webby.ws.context.ClientFrameType;
import io.webby.ws.meta.FrameMetadata;
import io.webby.ws.meta.TextSeparatorFrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertFrame.*;

public class AcceptingDifferentTest extends BaseWebsocketIntegrationTest {
    private static final TextSeparatorFrameMetadata TEXT_SEPARATOR_META = new TextSeparatorFrameMetadata();

    protected @NotNull AcceptingDifferent setupJson(@NotNull FrameType type, @NotNull FrameMetadata metadata) {
        return setupAgent(AcceptingDifferent.class, Marshal.JSON, type, metadata, FakeClients.DEFAULT);
    }

    protected @NotNull AcceptingDifferent setupJson(@NotNull FrameType serverType,
                                                    @NotNull FrameMetadata metadata,
                                                    @NotNull ClientFrameType clientType) {
        return setupAgent(AcceptingDifferent.class, Marshal.JSON, serverType, metadata, FakeClients.client(clientType));
    }

    @Test
    public void on_json_text_primitive() {
        AcceptingDifferent agent = setupJson(FrameType.TEXT_ONLY, TEXT_SEPARATOR_META);
        Queue<WebSocketFrame> frames = sendText("primitive 1 {'ch': 'a', 'bool': true}");
        assertTextFrames(frames, """
            1 0 {"s":"a-true"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('a').withBool(true));
    }

    @Test
    public void on_json_from_client_text_primitive() {
        AcceptingDifferent agent = setupJson(FrameType.FROM_CLIENT, TEXT_SEPARATOR_META);
        Queue<WebSocketFrame> frames = sendText("primitive 1 {'ch': 'a', 'bool': true}");
        assertTextFrames(frames, """
            1 0 {"s":"a-true"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('a').withBool(true));
    }

    @Test
    public void on_json_from_client_binary_primitive() {
        AcceptingDifferent agent = setupJson(FrameType.FROM_CLIENT, TEXT_SEPARATOR_META);
        Queue<WebSocketFrame> frames = sendBinary("primitive 1 {'ch': 'a', 'bool': true}");
        assertBinaryFrames(frames, """
            1 0 {"s":"a-true"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('a').withBool(true));
    }

    @Test
    public void on_json_text_string_not_null() {
        AcceptingDifferent agent = setupJson(FrameType.TEXT_ONLY, TEXT_SEPARATOR_META);
        Queue<WebSocketFrame> frames = sendText("str 111111111 {'s': 'aaa'}");
        Assertions.assertEquals(96352, new StringMessage("aaa").hashCode());
        assertTextFrames(frames, """
            111111111 0 {"i":96352,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new StringMessage("aaa"));
    }

    @Test
    public void on_json_binary_string_not_null() {
        AcceptingDifferent agent = setupJson(FrameType.BINARY_ONLY, TEXT_SEPARATOR_META);
        Queue<WebSocketFrame> frames = sendBinary("str 111111111 {'s': 'aaa'}");
        Assertions.assertEquals(96352, new StringMessage("aaa").hashCode());
        assertBinaryFrames(frames, """
            111111111 0 {"i":96352,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new StringMessage("aaa"));
    }

    @Test
    public void on_json_text_string_null() {
        AcceptingDifferent agent = setupJson(FrameType.TEXT_ONLY, TEXT_SEPARATOR_META);
        Queue<WebSocketFrame> frames = sendText("str 111111111 {'s': null}");
        assertTextFrames(frames);
        assertThat(agent.getIncoming()).containsExactly(new StringMessage(null));
    }

    @Test
    public void on_json_binary_string_null() {
        AcceptingDifferent agent = setupJson(FrameType.BINARY_ONLY, TEXT_SEPARATOR_META);
        Queue<WebSocketFrame> frames = sendBinary("str 111111111 {'s': null}");
        assertBinaryFrames(frames);
        assertThat(agent.getIncoming()).containsExactly(new StringMessage(null));
    }

    @Test
    public void compatibility_server_text_only_client_text() {
        AcceptingDifferent agent = setupJson(FrameType.TEXT_ONLY, TEXT_SEPARATOR_META, ClientFrameType.TEXT);

        Queue<WebSocketFrame> frames = sendText("primitive 777 {'ch': 'a', 'bool': true}");
        assertTextFrames(frames, """
            777 0 {"s":"a-true"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('a').withBool(true));

        assertBadFrame(sendBinary("primitive 777 {'ch': 'a', 'bool': true}"));
    }

    @Test
    public void compatibility_server_text_only_client_both() {
        assertClientDenied(() -> setupJson(FrameType.TEXT_ONLY, TEXT_SEPARATOR_META, ClientFrameType.BOTH));
    }

    @Test
    public void compatibility_server_text_only_client_binary() {
        assertClientDenied(() -> setupJson(FrameType.TEXT_ONLY, TEXT_SEPARATOR_META, ClientFrameType.BINARY));
    }

    @Test
    public void compatibility_server_text_only_client_any() {
        AcceptingDifferent agent = setupJson(FrameType.TEXT_ONLY, TEXT_SEPARATOR_META, ClientFrameType.ANY);

        Queue<WebSocketFrame> frames = sendText("primitive 777 {'ch': 'a', 'bool': false}");
        assertTextFrames(frames, """
            777 0 {"s":"a-false"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('a').withBool(false));

        assertBadFrame(sendBinary("primitive 777 {'ch': 'a', 'bool': true}"));
    }

    @Test
    public void compatibility_server_binary_only_client_binary() {
        AcceptingDifferent agent = setupJson(FrameType.BINARY_ONLY, TEXT_SEPARATOR_META, ClientFrameType.BINARY);

        Queue<WebSocketFrame> frames = sendBinary("primitive 777 {'ch': 'b', 'bool': true}");
        assertBinaryFrames(frames, """
            777 0 {"s":"b-true"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('b').withBool(true));

        assertBadFrame(sendText("primitive 777 {'ch': 'a', 'bool': true}"));
    }

    @Test
    public void compatibility_server_binary_only_client_both() {
        assertClientDenied(() -> setupJson(FrameType.BINARY_ONLY, TEXT_SEPARATOR_META, ClientFrameType.BOTH));
    }

    @Test
    public void compatibility_server_binary_only_client_text() {
        assertClientDenied(() -> setupJson(FrameType.BINARY_ONLY, TEXT_SEPARATOR_META, ClientFrameType.TEXT));
    }

    @Test
    public void compatibility_server_binary_only_client_any() {
        AcceptingDifferent agent = setupJson(FrameType.BINARY_ONLY, TEXT_SEPARATOR_META, ClientFrameType.ANY);

        Queue<WebSocketFrame> frames = sendBinary("primitive 777 {'ch': 'b', 'bool': false}");
        assertBinaryFrames(frames, """
            777 0 {"s":"b-false"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('b').withBool(false));

        assertBadFrame(sendText("primitive 777 {'ch': 'a', 'bool': true}"));
    }

    @Test
    public void compatibility_server_both_only_client_binary() {
        AcceptingDifferent agent = setupJson(FrameType.ALLOW_BOTH, TEXT_SEPARATOR_META, ClientFrameType.BINARY);

        Queue<WebSocketFrame> frames1 = sendText("primitive 777 {'ch': 'c', 'bool': true}");
        assertTextFrames(frames1, """
            777 0 {"s":"c-true"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('c').withBool(true));
        agent.getIncoming().clear();

        Queue<WebSocketFrame> frames2 = sendBinary("primitive 777 {'ch': 'c', 'bool': true}");
        assertBinaryFrames(frames2, """
            777 0 {"s":"c-true"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('c').withBool(true));
    }

    @Test
    public void compatibility_server_both_only_client_both() {
        AcceptingDifferent agent = setupJson(FrameType.ALLOW_BOTH, TEXT_SEPARATOR_META, ClientFrameType.BOTH);

        Queue<WebSocketFrame> frames1 = sendText("primitive 777 {'ch': 'c', 'bool': false}");
        assertTextFrames(frames1, """
            777 0 {"s":"c-false"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('c').withBool(false));
        agent.getIncoming().clear();

        Queue<WebSocketFrame> frames2 = sendBinary("primitive 777 {'ch': 'c', 'bool': false}");
        assertBinaryFrames(frames2, """
            777 0 {"s":"c-false"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('c').withBool(false));
    }

    @Test
    public void compatibility_server_both_only_client_text() {
        AcceptingDifferent agent = setupJson(FrameType.ALLOW_BOTH, TEXT_SEPARATOR_META, ClientFrameType.BOTH);

        Queue<WebSocketFrame> frames1 = sendText("primitive 777 {'ch': 'd', 'bool': false}");
        assertTextFrames(frames1, """
            777 0 {"s":"d-false"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('d').withBool(false));
        agent.getIncoming().clear();

        Queue<WebSocketFrame> frames2 = sendBinary("primitive 777 {'ch': 'd', 'bool': false}");
        assertBinaryFrames(frames2, """
            777 0 {"s":"d-false"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('d').withBool(false));
    }

    @Test
    public void compatibility_server_both_only_client_any() {
        AcceptingDifferent agent = setupJson(FrameType.ALLOW_BOTH, TEXT_SEPARATOR_META, ClientFrameType.ANY);

        Queue<WebSocketFrame> frames1 = sendText("primitive 777 {'ch': 'd', 'bool': true}");
        assertTextFrames(frames1, """
            777 0 {"s":"d-true"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('d').withBool(true));
        agent.getIncoming().clear();

        Queue<WebSocketFrame> frames2 = sendBinary("primitive 777 {'ch': 'd', 'bool': true}");
        assertBinaryFrames(frames2, """
            777 0 {"s":"d-true"}
        """.trim());
        assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('d').withBool(true));
    }
}
