package io.webby.netty.ws;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.ws.BaseRequestContext;
import io.webby.ws.MessageSender;
import io.webby.ws.convert.OutFrameConverter;
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
        WebSocketFrame frame = converter.toFrame(context, code, message);
        return send(frame);
    }

    @Override
    public @NotNull ChannelFuture sendFlushMessage(int code, @NotNull M message, @NotNull BaseRequestContext context) {
        assert converter != null :
            "Message converter is not initialized. Possible reasons: Agent is missing @WebsocketProtocol annotation";
        WebSocketFrame frame = converter.toFrame(context, code, message);
        return sendFlush(frame);
    }
}
