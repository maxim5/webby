package io.spbx.webby.demo.websockets.protocol;

import io.spbx.webby.demo.websockets.protocol.ExampleMessages.PrimitiveMessage;
import io.spbx.webby.demo.websockets.protocol.ExampleMessages.SimpleMessage;
import io.spbx.webby.demo.websockets.protocol.ExampleMessages.StringMessage;
import io.spbx.webby.url.annotate.Api;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.WebsocketProtocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Serve(url = "/ws/accept/messages", websocket = true)
@WebsocketProtocol(messages = SimpleMessage.class)
public class AcceptingDifferent {
    private final List<SimpleMessage> incoming = new ArrayList<>();

    @Api(id="primitive", version = "1.0")
    private @NotNull StringMessage onPrimitive(@NotNull PrimitiveMessage message) {
        incoming.add(message);
        return new StringMessage("%s-%s".formatted(message.ch, message.bool));
    }

    @Api(id="str", version = "1.0")
    private @Nullable PrimitiveMessage onString(@NotNull StringMessage message) {
        incoming.add(message);
        return message.s != null ? PrimitiveMessage.primitiveInt(message.hashCode()) : null;
    }

    public @NotNull List<SimpleMessage> getIncoming() {
        return incoming;
    }
}
