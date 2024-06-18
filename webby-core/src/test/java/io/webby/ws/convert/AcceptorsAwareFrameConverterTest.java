package io.webby.ws.convert;

import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.netty.ws.FrameConst.StatusCodes;
import io.webby.netty.ws.errors.BadFrameException;
import io.webby.netty.ws.errors.ClientDeniedException;
import io.webby.testing.FakeClients;
import io.webby.testing.Testing;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.ws.context.ClientFrameType;
import io.webby.ws.context.EmptyContext;
import io.webby.ws.convert.AcceptorsAwareFrameConverter.ConcreteFrameType;
import io.webby.ws.convert.InFrameConverter.ParsedFrameConsumer;
import io.webby.ws.impl.Acceptor;
import io.webby.ws.meta.FrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertFrame.assertBinaryFrame;
import static io.webby.testing.AssertFrame.assertTextFrame;
import static io.webby.testing.FakeFrames.binary;
import static io.webby.testing.FakeFrames.text;
import static io.spbx.util.testing.TestingBytes.asByteBuf;
import static io.spbx.util.testing.TestingBytes.asString;
import static io.webby.ws.convert.AcceptorsAwareFrameConverter.resolveFrameType;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("slow")
public class AcceptorsAwareFrameConverterTest {
    private static final Method DUMMY_METHOD = Testing.class.getDeclaredMethods()[0];
    private static final ParsedFrameConsumer<Object> EMPTY_CONSUMER = (accept, payload, ctx) -> {};

    @Test
    public void resolveFrameType_cases() {
        assertThat(resolveFrameType(ClientFrameType.TEXT, FrameType.TEXT_ONLY)).isEqualTo(ConcreteFrameType.TEXT);
        assertThat(resolveFrameType(ClientFrameType.ANY, FrameType.TEXT_ONLY)).isEqualTo(ConcreteFrameType.TEXT);
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.BINARY, FrameType.TEXT_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.BOTH, FrameType.TEXT_ONLY));

        assertThat(resolveFrameType(ClientFrameType.BINARY, FrameType.BINARY_ONLY)).isEqualTo(ConcreteFrameType.BINARY);
        assertThat(resolveFrameType(ClientFrameType.ANY, FrameType.BINARY_ONLY)).isEqualTo(ConcreteFrameType.BINARY);
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.TEXT, FrameType.BINARY_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.BOTH, FrameType.BINARY_ONLY));

        assertThat(resolveFrameType(ClientFrameType.TEXT, FrameType.FROM_CLIENT)).isEqualTo(ConcreteFrameType.TEXT);
        assertThat(resolveFrameType(ClientFrameType.ANY, FrameType.FROM_CLIENT)).isEqualTo(ConcreteFrameType.BOTH);  // temp
        assertThat(resolveFrameType(ClientFrameType.BINARY, FrameType.FROM_CLIENT)).isEqualTo(ConcreteFrameType.BINARY);
        assertThat(resolveFrameType(ClientFrameType.BOTH, FrameType.FROM_CLIENT)).isEqualTo(ConcreteFrameType.BOTH);

        assertThat(resolveFrameType(ClientFrameType.TEXT, FrameType.ALLOW_BOTH)).isEqualTo(ConcreteFrameType.BOTH);
        assertThat(resolveFrameType(ClientFrameType.BINARY, FrameType.ALLOW_BOTH)).isEqualTo(ConcreteFrameType.BOTH);
        assertThat(resolveFrameType(ClientFrameType.ANY, FrameType.ALLOW_BOTH)).isEqualTo(ConcreteFrameType.BOTH);
        assertThat(resolveFrameType(ClientFrameType.BOTH, FrameType.ALLOW_BOTH)).isEqualTo(ConcreteFrameType.BOTH);
    }

    @Test
    public void toMessage_allowBoth() {
        Acceptor acceptFoo = dummyMessageAcceptor("foo");
        Acceptor acceptBar = dummyMessageAcceptor("bar");

        AcceptorsAwareFrameConverter converter = setupConverter(FrameType.ALLOW_BOTH, acceptFoo, acceptBar);

        converter.toMessage(text("foo"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptFoo);
            assertThat(payload).isEqualTo("foo");
            assertThat(context.requestId()).isEqualTo(1);
            assertThat(context.isTextRequest()).isTrue();
        });

        converter.toMessage(binary("foo"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptFoo);
            assertThat(payload).isEqualTo("foo");
            assertThat(context.requestId()).isEqualTo(2);
            assertThat(context.isBinaryRequest()).isTrue();
        });

        converter.toMessage(text("bar"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptBar);
            assertThat(payload).isEqualTo("bar");
            assertThat(context.requestId()).isEqualTo(3);
            assertThat(context.isTextRequest()).isTrue();
        });

        converter.toMessage(binary("bar"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptBar);
            assertThat(payload).isEqualTo("bar");
            assertThat(context.requestId()).isEqualTo(4);
            assertThat(context.isBinaryRequest()).isTrue();
        });

        assertThrows(BadFrameException.class, () -> converter.toMessage(text("baz"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("baz"), EMPTY_CONSUMER));
    }

    @Test
    public void toMessage_onlyText() {
        Acceptor acceptFoo = dummyMessageAcceptor("foo");
        Acceptor acceptBar = dummyMessageAcceptor("bar");

        AcceptorsAwareFrameConverter converter = setupConverter(FrameType.TEXT_ONLY, acceptFoo, acceptBar);

        converter.toMessage(text("foo"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptFoo);
            assertThat(payload).isEqualTo("foo");
            assertThat(context.requestId()).isEqualTo(1);
            assertThat(context.isTextRequest()).isTrue();
        });

        converter.toMessage(text("bar"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptBar);
            assertThat(payload).isEqualTo("bar");
            assertThat(context.requestId()).isEqualTo(2);
            assertThat(context.isTextRequest()).isTrue();
        });

        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("foo"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("bar"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> converter.toMessage(text("baz"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("baz"), EMPTY_CONSUMER));
    }

    @Test
    public void toMessage_onlyBinary() {
        Acceptor acceptFoo = dummyMessageAcceptor("foo");
        Acceptor acceptBar = dummyMessageAcceptor("bar");

        AcceptorsAwareFrameConverter converter = setupConverter(FrameType.BINARY_ONLY, acceptFoo, acceptBar);

        converter.toMessage(binary("foo"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptFoo);
            assertThat(payload).isEqualTo("foo");
            assertThat(context.requestId()).isEqualTo(1);
            assertThat(context.isBinaryRequest()).isTrue();
        });

        converter.toMessage(binary("bar"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptBar);
            assertThat(payload).isEqualTo("bar");
            assertThat(context.requestId()).isEqualTo(2);
            assertThat(context.isBinaryRequest()).isTrue();
        });

        assertThrows(BadFrameException.class, () -> converter.toMessage(text("foo"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> converter.toMessage(text("bar"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> converter.toMessage(text("baz"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("baz"), EMPTY_CONSUMER));
    }

    @Test
    public void toMessage_acceptFrame() {
        Acceptor acceptTxt = dummyTextFrameAcceptor("txt");
        Acceptor acceptBin = dummyBinaryFrameAcceptor("bin");

        AcceptorsAwareFrameConverter converter = setupConverter(FrameType.ALLOW_BOTH, acceptTxt, acceptBin);

        converter.toMessage(text("txt"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptTxt);
            assertThat(payload).isEqualTo(text("txt"));
            assertThat(context.requestId()).isEqualTo(1);
            assertThat(context.isTextRequest()).isTrue();
        });
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("txt"), EMPTY_CONSUMER));

        converter.toMessage(binary("bin"), (acceptor, payload, context) -> {
            assertThat(acceptor).isEqualTo(acceptBin);
            assertThat(payload).isEqualTo(binary("bin"));
            assertThat(context.requestId()).isEqualTo(3);
            assertThat(context.isBinaryRequest()).isTrue();
        });
        assertThrows(BadFrameException.class, () -> converter.toMessage(text("bin"), EMPTY_CONSUMER));

        assertThrows(BadFrameException.class, () -> converter.toMessage(text("foo"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> converter.toMessage(binary("foo"), EMPTY_CONSUMER));
    }

    @Test
    public void toFrame_simple() {
        AcceptorsAwareFrameConverter converter = setupConverter(FrameType.ALLOW_BOTH);

        assertThat(converter.peekFrameType(EmptyContext.EMPTY_TEXT_CONTEXT)).isTrue();
        assertThat(converter.peekFrameType(EmptyContext.EMPTY_BINARY_CONTEXT)).isFalse();
        assertTextFrame(converter.toFrame(StatusCodes.OK, "ok", EmptyContext.EMPTY_TEXT_CONTEXT), "-1:0:ok");
        assertBinaryFrame(converter.toFrame(StatusCodes.OK, "ok", EmptyContext.EMPTY_BINARY_CONTEXT), "-1:0:ok");
    }

    private static @NotNull AcceptorsAwareFrameConverter setupConverter(@NotNull FrameType supportedType,
                                                                        @NotNull Acceptor ... acceptors) {
        return setupConverter(Marshal.AS_STRING, supportedType, acceptors);
    }

    private static @NotNull AcceptorsAwareFrameConverter setupConverter(@NotNull Marshal marshal,
                                                                        @NotNull FrameType supportedType,
                                                                        @NotNull Acceptor ... acceptors) {
        Injector injector = Testing.testStartup();
        AcceptorsAwareFrameConverter converter = new AcceptorsAwareFrameConverter(
            injector.getInstance(MarshallerFactory.class).getMarshaller(marshal),
            dummyFrameMetadata(),
            Maps.uniqueIndex(List.of(acceptors), Acceptor::id),
            supportedType
        );
        converter.onBeforeHandshake(FakeClients.DEFAULT);
        return converter;
    }

    private static @NotNull FrameMetadata dummyFrameMetadata() {
        AtomicInteger requestIdCounter = new AtomicInteger();
        return new FrameMetadata() {
            @Override
            public void parse(@NotNull ByteBuf frameContent, @NotNull MetadataConsumer consumer) {
                consumer.accept(frameContent, requestIdCounter.incrementAndGet(), frameContent);
            }

            @Override
            public @NotNull ByteBuf compose(long requestId, int code, byte @NotNull [] content) {
                return asByteBuf("%d:%d:%s".formatted(requestId, code, asString(content)));
            }
        };
    }

    private @NotNull Acceptor dummyMessageAcceptor(@NotNull String id) {
        return dummyAcceptor(id, String.class, false);
    }

    private @NotNull Acceptor dummyTextFrameAcceptor(@NotNull String id) {
        return dummyAcceptor(id, TextWebSocketFrame.class, true);
    }

    private @NotNull Acceptor dummyBinaryFrameAcceptor(@NotNull String id) {
        return dummyAcceptor(id, BinaryWebSocketFrame.class, true);
    }

    private static @NotNull Acceptor dummyAcceptor(@NotNull String id, @NotNull Class<?> type, boolean acceptsFrame) {
        return new Acceptor(asByteBuf(id), "1.0", type, DUMMY_METHOD, null, acceptsFrame, false, false);
    }
}
