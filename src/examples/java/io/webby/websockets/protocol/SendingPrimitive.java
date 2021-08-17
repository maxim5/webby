package io.webby.websockets.protocol;

import com.google.inject.Inject;
import io.webby.url.annotate.Serve;
import io.webby.url.annotate.WebsocketProtocol;
import io.webby.websockets.protocol.ExampleMessages.StringMessage;
import io.webby.ws.MessageSender;
import io.webby.ws.RequestContext;
import org.jetbrains.annotations.NotNull;

import static io.webby.websockets.protocol.ExampleMessages.PrimitiveMessage;
import static io.webby.websockets.protocol.ExampleMessages.SimpleMessage;

@Serve(url = "/ws/send/primitive", websocket = true)
@WebsocketProtocol(messages = SimpleMessage.class)
public class SendingPrimitive {
    @Inject private MessageSender<SimpleMessage> sender;

    public void onPrimitive(@NotNull PrimitiveMessage message) {
        sender.sendFlushMessage(message.withBool(true));
    }

    public void onString(@NotNull StringMessage message, @NotNull RequestContext context) {
        sender.sendFlushMessage(message.with("Ack %s".formatted(message.s)), context);
    }
}
