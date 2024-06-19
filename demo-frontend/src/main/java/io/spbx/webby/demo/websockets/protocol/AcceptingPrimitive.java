package io.spbx.webby.demo.websockets.protocol;

import io.spbx.webby.demo.websockets.protocol.ExampleMessages.PrimitiveMessage;
import io.spbx.webby.demo.websockets.protocol.ExampleMessages.SimpleMessage;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.WebsocketProtocol;
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
