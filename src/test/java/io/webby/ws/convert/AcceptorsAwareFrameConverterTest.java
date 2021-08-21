package io.webby.ws.convert;

import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.netty.ws.errors.BadFrameException;
import io.webby.netty.ws.errors.ClientDeniedException;
import io.webby.testing.FakeClients;
import io.webby.testing.Testing;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.ws.context.ClientFrameType;
import io.webby.ws.convert.AcceptorsAwareFrameConverter.ConcreteFrameType;
import io.webby.ws.impl.Acceptor;
import io.webby.ws.meta.FrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.webby.testing.FakeFrames.binary;
import static io.webby.testing.FakeFrames.text;
import static io.webby.testing.TestingBytes.asByteBuf;
import static io.webby.ws.convert.AcceptorsAwareFrameConverter.resolveFrameType;
import static org.junit.jupiter.api.Assertions.*;

public class AcceptorsAwareFrameConverterTest {
    private static final Method DUMMY_METHOD = Testing.class.getDeclaredMethods()[0];

    @Test
    public void resolveFrameType_cases() {
        assertEquals(ConcreteFrameType.TEXT, resolveFrameType(ClientFrameType.TEXT, FrameType.TEXT_ONLY));
        assertEquals(ConcreteFrameType.TEXT, resolveFrameType(ClientFrameType.ANY, FrameType.TEXT_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.BINARY, FrameType.TEXT_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.BOTH, FrameType.TEXT_ONLY));

        assertEquals(ConcreteFrameType.BINARY, resolveFrameType(ClientFrameType.BINARY, FrameType.BINARY_ONLY));
        assertEquals(ConcreteFrameType.BINARY, resolveFrameType(ClientFrameType.ANY, FrameType.BINARY_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.TEXT, FrameType.BINARY_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.BOTH, FrameType.BINARY_ONLY));

        assertEquals(ConcreteFrameType.TEXT, resolveFrameType(ClientFrameType.TEXT, FrameType.FROM_CLIENT));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.ANY, FrameType.FROM_CLIENT));  // temp
        assertEquals(ConcreteFrameType.BINARY, resolveFrameType(ClientFrameType.BINARY, FrameType.FROM_CLIENT));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.BOTH, FrameType.FROM_CLIENT));

        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.TEXT, FrameType.ALLOW_BOTH));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.BINARY, FrameType.ALLOW_BOTH));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.ANY, FrameType.ALLOW_BOTH));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.BOTH, FrameType.ALLOW_BOTH));
    }

    @Test
    public void toMessage_allowBoth() {
        Acceptor acceptFoo = dummyAcceptor("foo", false);
        Acceptor acceptBar = dummyAcceptor("bar", false);

        AcceptorsAwareFrameConverter converter = setupConverter(FrameType.ALLOW_BOTH, acceptFoo, acceptBar);

        converter.toMessage(text("foo"), (acceptor, payload, context) -> {
            assertEquals(acceptFoo, acceptor);
            assertEquals("foo", payload);
            assertEquals(1, context.requestId());
            assertTrue(context.isTextRequest());
        });

        converter.toMessage(binary("foo"), (acceptor, payload, context) -> {
            assertEquals(acceptFoo, acceptor);
            assertEquals("foo", payload);
            assertEquals(2, context.requestId());
            assertTrue(context.isBinaryRequest());
        });

        converter.toMessage(text("bar"), (acceptor, payload, context) -> {
            assertEquals(acceptBar, acceptor);
            assertEquals("bar", payload);
            assertEquals(3, context.requestId());
            assertTrue(context.isTextRequest());
        });

        converter.toMessage(binary("bar"), (acceptor, payload, context) -> {
            assertEquals(acceptBar, acceptor);
            assertEquals("bar", payload);
            assertEquals(4, context.requestId());
            assertTrue(context.isBinaryRequest());
        });

        assertThrows(BadFrameException.class, () -> converter.toMessage(text("baz"), (accept, payload, ctx) -> {}));
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("baz"), (accept, payload, ctx) -> {}));
    }

    @Test
    public void toMessage_onlyText() {
        Acceptor acceptFoo = dummyAcceptor("foo", false);
        Acceptor acceptBar = dummyAcceptor("bar", false);

        AcceptorsAwareFrameConverter converter = setupConverter(FrameType.TEXT_ONLY, acceptFoo, acceptBar);

        converter.toMessage(text("foo"), (acceptor, payload, context) -> {
            assertEquals(acceptFoo, acceptor);
            assertEquals("foo", payload);
            assertEquals(1, context.requestId());
            assertTrue(context.isTextRequest());
        });

        converter.toMessage(text("bar"), (acceptor, payload, context) -> {
            assertEquals(acceptBar, acceptor);
            assertEquals("bar", payload);
            assertEquals(2, context.requestId());
            assertTrue(context.isTextRequest());
        });

        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("foo"), (accept, payload, ctx) -> {}));
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("bar"), (accept, payload, ctx) -> {}));
        assertThrows(BadFrameException.class, () -> converter.toMessage(text("baz"), (accept, payload, ctx) -> {}));
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("baz"), (accept, payload, ctx) -> {}));
    }

    @Test
    public void toMessage_onlyBinary() {
        Acceptor acceptFoo = dummyAcceptor("foo", false);
        Acceptor acceptBar = dummyAcceptor("bar", false);

        AcceptorsAwareFrameConverter converter = setupConverter(FrameType.BINARY_ONLY, acceptFoo, acceptBar);

        converter.toMessage(binary("foo"), (acceptor, payload, context) -> {
            assertEquals(acceptFoo, acceptor);
            assertEquals("foo", payload);
            assertEquals(1, context.requestId());
            assertTrue(context.isBinaryRequest());
        });

        converter.toMessage(binary("bar"), (acceptor, payload, context) -> {
            assertEquals(acceptBar, acceptor);
            assertEquals("bar", payload);
            assertEquals(2, context.requestId());
            assertTrue(context.isBinaryRequest());
        });

        assertThrows(BadFrameException.class, () -> converter.toMessage(text("foo"), (accept, payload, ctx) -> {}));
        assertThrows(BadFrameException.class, () -> converter.toMessage(text("bar"), (accept, payload, ctx) -> {}));
        assertThrows(BadFrameException.class, () -> converter.toMessage(text("baz"), (accept, payload, ctx) -> {}));
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("baz"), (accept, payload, ctx) -> {}));
    }

    private static @NotNull AcceptorsAwareFrameConverter setupConverter(@NotNull FrameType supportedType,
                                                                        @NotNull Acceptor ... acceptors) {
        return setupConverter(Marshal.AS_STRING, supportedType, acceptors);
    }

    private static @NotNull AcceptorsAwareFrameConverter setupConverter(@NotNull Marshal marshal,
                                                                        @NotNull FrameType supportedType,
                                                                        @NotNull Acceptor ... acceptors) {
        Injector injector = Testing.testStartupNoHandlers();

        AtomicInteger requestIdCounter = new AtomicInteger();
        FrameMetadata metadata = new FrameMetadata() {
            @Override
            public void parse(@NotNull ByteBuf frameContent, @NotNull MetadataConsumer consumer) {
                consumer.accept(frameContent, requestIdCounter.incrementAndGet(), frameContent);
            }

            @Override
            public @NotNull ByteBuf compose(long requestId, int code, byte @NotNull [] content) {
                return null;
            }
        };

        AcceptorsAwareFrameConverter converter = new AcceptorsAwareFrameConverter(
                injector.getInstance(MarshallerFactory.class).getMarshaller(marshal),
                metadata,
                Maps.uniqueIndex(List.of(acceptors), Acceptor::id),
                supportedType
        );
        converter.onBeforeHandshake(FakeClients.DEFAULT);

        return converter;
    }

    private static @NotNull Acceptor dummyAcceptor(@NotNull String id, boolean acceptsFrame) {
        return new Acceptor(asByteBuf(id), "1.0", String.class, DUMMY_METHOD, null, acceptsFrame, false, false);
    }
}
