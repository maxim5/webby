package io.spbx.webby.demo.websockets.lowlevel;

import com.google.inject.Inject;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.spbx.webby.netty.ws.sender.Sender;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.ws.context.RequestContext;
import org.jetbrains.annotations.NotNull;

import static io.spbx.webby.netty.ws.sender.Sender.text;

@Serve(url = "/ws/ll/accept/context", websocket = true)
public class LLAcceptingContext {
    @Inject private Sender sender;

    public void onTextWithContext(@NotNull TextWebSocketFrame frame, @NotNull RequestContext context) {
        sender.sendFlush(text("Ack %s %s".formatted(frame.text(), context.clientInfo().versionOrNull())));
    }
}
