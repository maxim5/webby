package io.webby.ws.convert;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.netty.marshal.BinaryMarshaller;
import io.webby.netty.ws.errors.BadFrameException;
import io.webby.netty.ws.errors.ClientDeniedException;
import io.webby.url.annotate.FrameType;
import io.webby.ws.context.BaseRequestContext;
import io.webby.ws.context.ClientFrameType;
import io.webby.ws.context.ClientInfo;
import io.webby.ws.context.RequestContext;
import io.webby.ws.impl.Acceptor;
import io.webby.ws.lifecycle.AgentLifecycle;
import io.webby.ws.meta.FrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Map;

public final class AcceptorsAwareFrameConverter implements FrameConverter<Object>, AgentLifecycle {
    private final @NotNull BinaryMarshaller marshaller;
    private final @NotNull FrameMetadata metadata;
    private final @NotNull Map<ByteBuf, Acceptor> acceptors;
    private final @NotNull FrameType supportedType;

    private ClientInfo clientInfo;
    private ConcreteFrameType concreteFrameType = null;

    public AcceptorsAwareFrameConverter(@NotNull BinaryMarshaller marshaller,
                                        @NotNull FrameMetadata metadata,
                                        @NotNull Map<ByteBuf, Acceptor> acceptors,
                                        @NotNull FrameType supportedType) {
        this.marshaller = marshaller;
        this.metadata = metadata;
        this.acceptors = acceptors;
        this.supportedType = supportedType;
    }

    @Override
    public void onBeforeHandshake(@NotNull ClientInfo clientInfo) {
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
                    case ANY -> ConcreteFrameType.BOTH;  // here it should collapse to TEXT and inform the client.
                    case BOTH -> ConcreteFrameType.BOTH;
                };
            case ALLOW_BOTH -> ConcreteFrameType.BOTH;
        };
    }

    @Override
    public void toMessage(@NotNull WebSocketFrame frame, @NotNull ParsedFrameConsumer<Object> success) {
        assert concreteFrameType != null : "Converter is not initialized";
        if (concreteFrameType == ConcreteFrameType.TEXT && !(frame instanceof TextWebSocketFrame)) {
            throw new BadFrameException("Unsupported frame received: %s, expected text".formatted(frame.getClass()));
        }
        if (concreteFrameType == ConcreteFrameType.BINARY && !(frame instanceof BinaryWebSocketFrame)) {
            throw new BadFrameException("Unsupported frame received: %s, expected binary".formatted(frame.getClass()));
        }

        ByteBuf frameContent = Unpooled.wrappedBuffer(frame.content());
        metadata.parse(frameContent, (acceptorId, requestId, content) -> {
            if (acceptorId == null) {
                throw new BadFrameException("Failed to parse acceptor id");
            }
            Acceptor acceptor = acceptors.get(acceptorId);
            if (acceptor == null) {
                throw new BadFrameException("Acceptor not found: %s".formatted(acceptorId));    // not readable!
            }

            RequestContext context = new RequestContext(requestId, frame, clientInfo);
            if (acceptor.acceptsFrame()) {
                if (acceptor.type().isAssignableFrom(frame.getClass())) {
                    success.accept(acceptor, frame, context);
                } else {
                    throw new BadFrameException("Acceptor doesn't expect %s".formatted(frame.getClass()));
                }
            } else {
                Object payload = marshaller.readByteBuf(content, acceptor.type());
                success.accept(acceptor, payload, context);
            }
        });
    }

    @Override
    public Boolean peekFrameType(@NotNull BaseRequestContext context) {
        return concreteFrameType == ConcreteFrameType.TEXT || (concreteFrameType == ConcreteFrameType.BOTH && context.isTextRequest());
    }

    @Override
    public @NotNull WebSocketFrame toFrame(int code, @NotNull Object message, @NotNull BaseRequestContext context) {
        assert concreteFrameType != null : "Converter is not initialized";

        ByteBuf byteBuf = metadata.compose(context.requestId(), code, marshaller.writeBytes(message));
        return switch (concreteFrameType) {
            case TEXT -> new TextWebSocketFrame(byteBuf);
            case BINARY -> new BinaryWebSocketFrame(byteBuf);
            case BOTH -> context.isTextRequest() ? new TextWebSocketFrame(byteBuf) : new BinaryWebSocketFrame(byteBuf);
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
