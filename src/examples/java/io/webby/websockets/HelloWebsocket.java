package io.webby.websockets;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.webby.url.annotate.ServeWebsocket;
import org.jetbrains.annotations.NotNull;

@ServeWebsocket(url = "/ws/hello")
public class HelloWebsocket {
    public @NotNull TextWebSocketFrame onText(@NotNull TextWebSocketFrame frame) {
        System.out.println("Hello text " + frame);
        return new TextWebSocketFrame("Ack " + frame.text());
    }

    public void onBinary(@NotNull BinaryWebSocketFrame frame) {
        System.out.println("Hello bin " + frame);
    }
}
