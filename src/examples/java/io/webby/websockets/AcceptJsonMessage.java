package io.webby.websockets;

import io.webby.url.annotate.ServeWebsocket;
import io.webby.websockets.ExampleMessages.PrimitiveMessage;
import io.webby.websockets.ExampleMessages.SimpleMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@ServeWebsocket(url = "/ws/accept/json", messages = SimpleMessage.class)
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
