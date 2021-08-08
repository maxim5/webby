package io.webby.websockets;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.webby.url.annotate.ServeWebsocket;
import org.jetbrains.annotations.NotNull;

@ServeWebsocket(url = "/ws/hello")
public class HelloWebsocket {
    public void onText(@NotNull TextWebSocketFrame frame) {

    }

    public void onBinary(@NotNull BinaryWebSocketFrame frame) {

    }
}
