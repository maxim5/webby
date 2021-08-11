package io.webby.websockets;

import com.google.inject.Inject;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.webby.url.ws.Sender;
import io.webby.url.annotate.ServeWebsocket;
import org.jetbrains.annotations.NotNull;

@ServeWebsocket(url = "/ws/sending")
public class Sending {
    @Inject private Sender sender;

    public void onText(@NotNull TextWebSocketFrame frame) {
        sender.accept(new TextWebSocketFrame("Ack %s".formatted(frame.text())));
    }
}
