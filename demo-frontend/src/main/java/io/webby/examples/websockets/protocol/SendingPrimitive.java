package io.webby.examples.websockets.protocol;

import com.google.inject.Inject;
import io.webby.url.annotate.Serve;
import io.webby.url.annotate.WebsocketProtocol;
import io.webby.examples.websockets.protocol.ExampleMessages.StringMessage;
import io.webby.netty.ws.sender.MessageSender;
import io.webby.ws.context.RequestContext;
import org.jetbrains.annotations.NotNull;

import static io.webby.examples.websockets.protocol.ExampleMessages.PrimitiveMessage;
import static io.webby.examples.websockets.protocol.ExampleMessages.SimpleMessage;

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
