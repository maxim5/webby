package io.webby.websockets;

import com.google.common.truth.Truth;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.testing.AssertFrame;
import io.webby.testing.BaseWebsocketIntegrationTest;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.websockets.ExampleMessages.PrimitiveMessage;
import io.webby.websockets.ExampleMessages.StringMessage;
import io.webby.ws.meta.FrameMetadata;
import io.webby.ws.meta.TextSeparatorFrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Queue;

public class AcceptDifferentMessagesTest extends BaseWebsocketIntegrationTest {
    protected @NotNull AcceptDifferentMessages setupJson(@NotNull FrameType type, @NotNull FrameMetadata metadata) {
        return setupAgent(AcceptDifferentMessages.class, Marshal.JSON, type, metadata);
    }

    @Test
    public void on_json_text_primitive() {
        AcceptDifferentMessages agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendText("primitive 1 {'ch': 'a', 'bool': true}");
        AssertFrame.assertTextFrames(frames, """
            1 0 {"s":"a-true"}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(new PrimitiveMessage().withChar('a').withBool(true));
    }

    @Test
    public void on_json_text_string_not_null() {
        AcceptDifferentMessages agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendText("str 111111111 {'s': 'aaa'}");
        Assertions.assertEquals(96352, new StringMessage("aaa").hashCode());
        AssertFrame.assertTextFrames(frames, """
            111111111 0 {"i":96352,"l":0,"b":0,"s":0,"ch":"\\u0000","f":0.0,"d":0.0,"bool":false}
        """.trim());
        Truth.assertThat(agent.getIncoming()).containsExactly(new StringMessage("aaa"));
    }

    @Test
    public void on_json_text_string_null() {
        AcceptDifferentMessages agent = setupJson(FrameType.TEXT_ONLY, new TextSeparatorFrameMetadata());
        Queue<WebSocketFrame> frames = sendText("str 111111111 {'s': null}");
        AssertFrame.assertTextFrames(frames);
        Truth.assertThat(agent.getIncoming()).containsExactly(new StringMessage(null));
    }
}
