package io.spbx.webby.ws.convert;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.ws.context.RequestContext;
import io.spbx.webby.ws.impl.Acceptor;
import org.jetbrains.annotations.NotNull;

public interface InFrameConverter<M> {
    void toMessage(@NotNull WebSocketFrame frame, @NotNull ParsedFrameConsumer<M> success);

    interface ParsedFrameConsumer<M> {
        void accept(@NotNull Acceptor acceptor, @NotNull M payload, @NotNull RequestContext context);
    }
}
