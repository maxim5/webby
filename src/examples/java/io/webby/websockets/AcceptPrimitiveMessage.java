package io.webby.websockets;

import io.webby.url.annotate.Serve;
import io.webby.url.annotate.WebsocketProtocol;
import io.webby.websockets.ExampleMessages.PrimitiveMessage;
import io.webby.websockets.ExampleMessages.SimpleMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Serve(url = "/ws/accept/json", websocket = true)
@WebsocketProtocol(messages = SimpleMessage.class)
public class AcceptPrimitiveMessage {
    private final List<SimpleMessage> incoming = new ArrayList<>();

    public PrimitiveMessage onPrimitive(@NotNull PrimitiveMessage message) {
        incoming.add(message);
        return message;
    }

    public @NotNull List<SimpleMessage> getIncoming() {
        return incoming;
    }
}
