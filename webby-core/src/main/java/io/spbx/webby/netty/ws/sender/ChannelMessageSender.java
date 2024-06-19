package io.spbx.webby.netty.ws.sender;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.ws.context.BaseRequestContext;
import io.spbx.webby.ws.convert.OutFrameConverter;
import org.jetbrains.annotations.NotNull;

public class ChannelMessageSender<M> extends ChannelSender implements MessageSender<M> {
    private OutFrameConverter<M> converter;

    @Override
    public void onConverter(@NotNull OutFrameConverter<M> converter) {
        this.converter = converter;
    }

    @Override
    public @NotNull ChannelFuture sendMessage(int code, @NotNull M message, @NotNull BaseRequestContext context) {
        assert converter != null :
            "Message converter is not initialized. Possible reasons: Agent is missing @WebsocketProtocol annotation";
        WebSocketFrame frame = converter.toFrame(code, message, context);
        return send(frame);
    }

    @Override
    public @NotNull ChannelFuture sendFlushMessage(int code, @NotNull M message, @NotNull BaseRequestContext context) {
        assert converter != null :
            "Message converter is not initialized. Possible reasons: Agent is missing @WebsocketProtocol annotation";
        WebSocketFrame frame = converter.toFrame(code, message, context);
        return sendFlush(frame);
    }
}
