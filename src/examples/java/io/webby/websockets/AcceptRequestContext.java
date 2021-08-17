package io.webby.websockets;

import io.webby.url.annotate.Serve;
import io.webby.url.annotate.WebsocketProtocol;
import io.webby.websockets.ExampleMessages.SimpleMessage;
import io.webby.websockets.ExampleMessages.StringMessage;
import io.webby.ws.RequestContext;
import org.jetbrains.annotations.NotNull;

@Serve(url = "/ws/accept/primitive", websocket = true)
@WebsocketProtocol(messages = SimpleMessage.class)
public class AcceptRequestContext {
    public @NotNull StringMessage onString(@NotNull StringMessage message, @NotNull RequestContext context) {
        return new StringMessage("%s-%s-%s".formatted(message.s, context.requestId(), context.clientInfo().versionOrNull()));
    }
}
