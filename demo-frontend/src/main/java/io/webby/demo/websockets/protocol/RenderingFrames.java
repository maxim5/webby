package io.webby.demo.websockets.protocol;

import io.webby.demo.websockets.protocol.ExampleMessages.StringMessage;
import io.webby.url.annotate.Render;
import io.webby.url.annotate.Serve;
import io.webby.url.annotate.View;
import io.webby.url.annotate.WebsocketProtocol;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.webby.demo.websockets.protocol.ExampleMessages.SimpleMessage;

@Serve(url = "/ws/render/freemarker", render = Render.FREEMARKER, websocket = true)
@WebsocketProtocol(messages = SimpleMessage.class)
public class RenderingFrames {
    @View(template = "freemarker/message.ftl")
    @NotNull Map<String, Object> onStr(@NotNull StringMessage message) {
        return Map.of("message", message.s);
    }
}
