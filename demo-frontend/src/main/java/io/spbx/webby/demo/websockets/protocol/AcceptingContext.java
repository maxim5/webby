package io.spbx.webby.demo.websockets.protocol;

import io.spbx.webby.demo.websockets.protocol.ExampleMessages.SimpleMessage;
import io.spbx.webby.demo.websockets.protocol.ExampleMessages.StringMessage;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.WebsocketProtocol;
import io.spbx.webby.ws.context.RequestContext;
import org.jetbrains.annotations.NotNull;

@Serve(url = "/ws/accept/context", websocket = true)
@WebsocketProtocol(messages = SimpleMessage.class)
public class AcceptingContext {
    public @NotNull StringMessage onString(@NotNull StringMessage message, @NotNull RequestContext context) {
        return new StringMessage("%s-%s-%s".formatted(message.s, context.requestId(), context.clientInfo().versionOrNull()));
    }
}
