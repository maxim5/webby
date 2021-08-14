package io.webby.url.ws;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.marshal.BinaryMarshaller;
import io.webby.url.annotate.FrameType;
import io.webby.url.ws.meta.FrameMetadata;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public record AcceptorsAwareFrameConverter(@NotNull BinaryMarshaller marshaller,
                                           @NotNull FrameMetadata metadata,
                                           @NotNull Map<ByteBuf, Acceptor> acceptors,
                                           @NotNull FrameType outFrameType,
                                           @NotNull Charset charset) implements FrameConverter<Object> {
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
        return switch (outFrameType) {
            case TEXT -> new TextWebSocketFrame(byteBuf);
            case BINARY -> new BinaryWebSocketFrame(byteBuf);
        };
    }
}
