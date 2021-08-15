package io.webby.ws.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.marshal.BinaryMarshaller;
import io.webby.netty.ws.errors.BadFrameException;
import io.webby.netty.ws.errors.ClientDeniedException;
import io.webby.url.annotate.FrameType;
import io.webby.ws.ClientFrameType;
import io.webby.ws.ClientInfo;
import io.webby.ws.lifecycle.AgentLifecycle;
import io.webby.ws.meta.FrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public final class AcceptorsAwareFrameConverter implements FrameConverter<Object>, AgentLifecycle {
    private final @NotNull BinaryMarshaller marshaller;
    private final @NotNull FrameMetadata metadata;
    private final @NotNull Map<ByteBuf, Acceptor> acceptors;
    private final @NotNull FrameType supportedType;
    private final @NotNull Charset charset;

    private ClientInfo clientInfo;
    private ConcreteFrameType concreteFrameType = null;

    public AcceptorsAwareFrameConverter(@NotNull BinaryMarshaller marshaller,
                                        @NotNull FrameMetadata metadata,
                                        @NotNull Map<ByteBuf, Acceptor> acceptors,
                                        @NotNull FrameType supportedType,
                                        @NotNull Charset charset) {
        this.marshaller = marshaller;
        this.metadata = metadata;
        this.acceptors = acceptors;
        this.supportedType = supportedType;
        this.charset = charset;
    }

    @Override
    public void onConnectionAttempt(@NotNull ClientInfo clientInfo) {
        this.clientInfo = clientInfo;

        ClientFrameType clientType = clientInfo.preferredType().orElse(ClientFrameType.ANY);
        concreteFrameType = resolveFrameType(clientType, supportedType);
    }

    @VisibleForTesting
    static @NotNull ConcreteFrameType resolveFrameType(@NotNull ClientFrameType clientType, @NotNull FrameType supportedType) {
        return switch (supportedType) {
            case TEXT_ONLY -> {
                if (clientType != ClientFrameType.TEXT && clientType != ClientFrameType.ANY) {
                    throw new ClientDeniedException("Unsupported type: client=%s supported=%s".formatted(clientType, supportedType));
                }
                yield ConcreteFrameType.TEXT;
            }
            case BINARY_ONLY -> {
                if (clientType != ClientFrameType.BINARY && clientType != ClientFrameType.ANY) {
                    throw new ClientDeniedException("Unsupported type: client=%s supported=%s".formatted(clientType, supportedType));
                }
                yield ConcreteFrameType.BINARY;
            }
            case FROM_CLIENT ->
                switch (clientType) {
                    case TEXT -> ConcreteFrameType.TEXT;
                    case BINARY -> ConcreteFrameType.BINARY;
                    case ANY -> ConcreteFrameType.BOTH;
                    case BOTH -> ConcreteFrameType.BOTH;
                };
            case BOTH -> ConcreteFrameType.BOTH;
        };
    }

    @Override
    public void toMessage(@NotNull WebSocketFrame frame, @NotNull Consumer<Object> success, @NotNull Runnable failure) {
        assert concreteFrameType != null : "Converter is not initialized";
        if (concreteFrameType == ConcreteFrameType.TEXT && !(frame instanceof TextWebSocketFrame)) {
            throw new BadFrameException("Unsupported frame received: %s, expected text".formatted(frame.getClass()));
        }
        if (concreteFrameType == ConcreteFrameType.BINARY && !(frame instanceof BinaryWebSocketFrame)) {
            throw new BadFrameException("Unsupported frame received: %s, expected binary".formatted(frame.getClass()));
        }

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
        assert concreteFrameType != null : "Converter is not initialized";

        ByteBuf byteBuf = metadata.compose(requestId, code, marshaller.writeBytes(message, charset));
        return switch (concreteFrameType) {
            case TEXT -> new TextWebSocketFrame(byteBuf);
            case BINARY -> new BinaryWebSocketFrame(byteBuf);
            case BOTH -> new TextWebSocketFrame(byteBuf);  // TODO: take from request (thread-local map?)
        };
    }

    @Override
    public String toString() {
        return "AcceptorsAwareFrameConverter[marshaller=%s, metadata=%s, acceptors=%s, frameType=%s]"
                .formatted(marshaller, metadata, acceptors, supportedType);
    }

    private enum ConcreteFrameType {
        TEXT,
        BINARY,
        BOTH
    }
}
