package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static io.webby.testing.OkAsserts.describe;

public class DefaultWebSocketListener extends WebSocketListener {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final List<Object> messages = new ArrayList<>();
    private final AtomicReference<Throwable> error = new AtomicReference<>();

    public @NotNull List<Object> messages() {
        return messages;
    }

    public @Nullable Throwable error() {
        return error.get();
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        log.at(Level.INFO).log("[WS:%s] onOpen:\n%s", webSocket, describe(response));
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable cause, @Nullable Response response) {
        log.at(Level.SEVERE).withCause(cause).log("[WS:%s] onFailure:\n%s", webSocket, describe(response));
        error.set(cause);
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        log.at(Level.INFO).log("[WS:%s] onMessage: text=`%s`", webSocket, text);
        messages.add(text);
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        log.at(Level.INFO).log("[WS:%s] onMessage: bytes=`%s`", webSocket, bytes);
        messages.add(bytes);
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.at(Level.INFO).log("[WS:%s] onClosing: %d %s", webSocket, code, reason);
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.at(Level.INFO).log("[WS:%s] onClosed: %d %s", webSocket, code, reason);
    }
}
