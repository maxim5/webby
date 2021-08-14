package io.webby.websockets;

import io.webby.url.annotate.Ws;
import io.webby.url.annotate.WsProtocol;
import io.webby.websockets.ExampleMessages.PrimitiveMessage;
import io.webby.websockets.ExampleMessages.SimpleMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Ws(url = "/ws/accept/json")
@WsProtocol(messages = SimpleMessage.class)
public class AcceptJsonMessage {
    private final List<SimpleMessage> incoming = new ArrayList<>();

    public PrimitiveMessage onPrimitive(@NotNull PrimitiveMessage message) {
        incoming.add(message);
        return message;
    }

    public @NotNull List<SimpleMessage> getIncoming() {
        return incoming;
    }
}
