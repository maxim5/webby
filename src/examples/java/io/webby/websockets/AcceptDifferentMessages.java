package io.webby.websockets;

import io.webby.url.annotate.Api;
import io.webby.url.annotate.Serve;
import io.webby.url.annotate.WebsocketProtocol;
import io.webby.websockets.ExampleMessages.PrimitiveMessage;
import io.webby.websockets.ExampleMessages.SimpleMessage;
import io.webby.websockets.ExampleMessages.StringMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Serve(url = "/ws/accept/messages", websocket = true)
@WebsocketProtocol(messages = SimpleMessage.class)
public class AcceptDifferentMessages {
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
