package io.webby.url.ws;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.function.Consumer;

public interface Sender extends Consumer<WebSocketFrame> {
}
