package io.spbx.webby.demo.websockets.protocol;

import com.google.inject.Inject;
import io.spbx.webby.demo.websockets.protocol.ExampleMessages.StringMessage;
import io.spbx.webby.netty.ws.sender.MessageSender;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.WebsocketProtocol;
import io.spbx.webby.ws.context.RequestContext;
import org.jetbrains.annotations.NotNull;

import static io.spbx.webby.demo.websockets.protocol.ExampleMessages.PrimitiveMessage;
import static io.spbx.webby.demo.websockets.protocol.ExampleMessages.SimpleMessage;

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
