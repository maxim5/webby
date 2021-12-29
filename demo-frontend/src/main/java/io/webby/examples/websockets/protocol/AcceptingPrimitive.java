package io.webby.examples.websockets.protocol;

import io.webby.url.annotate.Serve;
import io.webby.url.annotate.WebsocketProtocol;
import io.webby.examples.websockets.protocol.ExampleMessages.PrimitiveMessage;
import io.webby.examples.websockets.protocol.ExampleMessages.SimpleMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Serve(url = "/ws/accept/primitive", websocket = true)
@WebsocketProtocol(messages = SimpleMessage.class)
public class AcceptingPrimitive {
    private final List<SimpleMessage> incoming = new ArrayList<>();

    public @NotNull PrimitiveMessage onPrimitive(@NotNull PrimitiveMessage message) {
        incoming.add(message);
        return message;
    }

    public @NotNull List<SimpleMessage> getIncoming() {
        return incoming;
    }
}
