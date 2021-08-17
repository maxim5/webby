package io.webby.websockets.lowlevel;

import com.google.inject.Inject;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.webby.url.annotate.Serve;
import io.webby.ws.RequestContext;
import io.webby.ws.Sender;
import org.jetbrains.annotations.NotNull;

import static io.webby.ws.Sender.text;

@Serve(url = "/ws/ll/accept/context", websocket = true)
public class LLAcceptingContext {
    @Inject private Sender sender;

    public void onTextWithContext(@NotNull TextWebSocketFrame frame, @NotNull RequestContext context) {
        sender.sendFlush(text("Ack %s %s".formatted(frame.text(), context.clientInfo().versionOrNull())));
    }
}
