package io.webby.url.ws;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.marshal.BinaryMarshaller;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public record AcceptorsAwareFrameConverter(@NotNull BinaryMarshaller marshaller,
                                           @NotNull FrameMetaReader metaReader,
                                           @NotNull Map<ByteBuf, Acceptor> acceptors,
                                           @NotNull Charset charset) implements FrameConverter<Object> {
    @Override
    public void toMessage(@NotNull WebSocketFrame frame, @NotNull Consumer<Object> success, @NotNull Runnable failure) {
        ByteBuf content = Unpooled.wrappedBuffer(frame.content());
        metaReader.readMeta(content, (acceptorId, requestId) ->
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
    public @NotNull WebSocketFrame toFrame(@NotNull Object message, long requestId) {
        byte[] requestBytes = Longs.toByteArray(requestId);
        byte[] payloadBytes = marshaller.writeBytes(message, charset);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(requestBytes, payloadBytes);
        return new BinaryWebSocketFrame(byteBuf);
    }
}
