package io.webby.url.ws;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.marshal.BinaryMarshaller;
import io.webby.url.annotate.FrameType;
import io.webby.url.ws.lifecycle.AgentLifecycle;
import io.webby.url.ws.meta.FrameMetadata;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public final class AcceptorsAwareFrameConverter implements FrameConverter<Object>, AgentLifecycle {
    private final @NotNull BinaryMarshaller marshaller;
    private final @NotNull FrameMetadata metadata;
    private final @NotNull Map<ByteBuf, Acceptor> acceptors;
    private final @NotNull FrameType frameType;
    private final @NotNull Charset charset;

    public AcceptorsAwareFrameConverter(@NotNull BinaryMarshaller marshaller,
                                        @NotNull FrameMetadata metadata,
                                        @NotNull Map<ByteBuf, Acceptor> acceptors,
                                        @NotNull FrameType frameType,
                                        @NotNull Charset charset) {
        this.marshaller = marshaller;
        this.metadata = metadata;
        this.acceptors = acceptors;
        this.frameType = frameType;
        this.charset = charset;
    }

    @Override
    public void onChannelConnected(@NotNull Channel channel) {
    }

    @Override
    public void toMessage(@NotNull WebSocketFrame frame, @NotNull Consumer<Object> success, @NotNull Runnable failure) {
        ByteBuf frameContent = Unpooled.wrappedBuffer(frame.content());
        metadata.parse(frameContent, (acceptorId, requestId, content) ->
                Optional.ofNullable(acceptorId)
                        .map(acceptors::get)
                        .ifPresentOrElse(acceptor -> {
                            if (acceptor.acceptsFrame()) {
                                if (acceptor.type().isAssignableFrom(frame.getClass())) {
                                    success.accept(acceptor, requestId, frame);
                                } else {
                                    failure.run();
                                }
                            } else {
                                Object payload = marshaller.readByteBuf(content, acceptor.type(), charset);
                                success.accept(acceptor, requestId, payload);
                            }
                        }, failure));
    }

    @Override
    public @NotNull WebSocketFrame toFrame(long requestId, int code, @NotNull Object message) {
        ByteBuf byteBuf = metadata.compose(requestId, code, marshaller.writeBytes(message, charset));
        return switch (frameType) {
            case TEXT -> new TextWebSocketFrame(byteBuf);
            case BINARY -> new BinaryWebSocketFrame(byteBuf);
        };
    }

    @Override
    public String toString() {
        return "AcceptorsAwareFrameConverter[marshaller=%s, metadata=%s, acceptors=%s, frameType=%s]"
                .formatted(marshaller, metadata, acceptors, frameType);
    }
}
