package io.webby.websockets;

import com.google.common.flogger.FluentLogger;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.webby.url.annotate.Ws;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

@Ws(url = "/ws/hello")
public class HelloWebsocket {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final List<Object> frames = new ArrayList<>();

    public @NotNull TextWebSocketFrame onText(@NotNull TextWebSocketFrame frame) {
        frames.add(frame.text());
        log.at(Level.INFO).log("onText: %s", frame.text());
        return new TextWebSocketFrame("Ack %s".formatted(frame.text()));
    }

    public void onBinary(@NotNull BinaryWebSocketFrame frame) {
        frames.add(frame.content().retain());
        log.at(Level.INFO).log("onBinary: %s", Arrays.toString(frame.content().array()));
    }

    public List<Object> getFrames() {
        return frames;
    }
}
