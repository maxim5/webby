package io.webby.testing;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;
import io.webby.app.AppSettings;
import io.webby.netty.dispatch.ws.NettyWebsocketHandler;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.util.base.Unchecked;
import io.webby.ws.context.ClientInfo;
import io.webby.ws.impl.AgentEndpoint;
import io.webby.ws.impl.WebsocketRouter;
import io.webby.ws.meta.FrameMetadata;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

import static io.webby.util.base.EasyCast.castAny;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BaseWebsocketIntegrationTest extends BaseChannelTest {
    protected <T> @NotNull WebsocketSetup<T> testSetup(@NotNull Class<T> klass) {
        return testSetup(klass, settings -> {});
    }

    protected <T> @NotNull WebsocketSetup<T> testSetup(@NotNull Class<T> klass,
                                                       @NotNull Consumer<AppSettings> consumer,
                                                       @NotNull Module... modules) {
        Injector injector = Testing.testStartup(
                DEFAULT_SETTINGS
                        .andThen(settings -> settings.handlerFilter().setSingleClassOnly(klass))
                        .andThen(consumer),
                modules
        );
        return new WebsocketSetup<>(injector, klass);
    }

    protected class WebsocketSetup<T> extends SingleTargetSetup<T> {
        public WebsocketSetup(@NotNull Injector injector, @NotNull Class<T> klass) {
            super(injector, klass);
        }

        public @NotNull T initAgent() {
            return initAgent(FakeClients.DEFAULT);
        }

        public @NotNull T initAgent(@NotNull ClientInfo clientInfo) {
            AgentEndpoint endpoint = injector.getInstance(WebsocketRouter.class).findAgentEndpointByClass(klass);
            assertNotNull(endpoint, "No Endpoint found for: %s".formatted(klass));
            NettyWebsocketHandler handler = new NettyWebsocketHandler(endpoint, clientInfo);
            injector.injectMembers(handler);
            channel = new EmbeddedChannel(handler);
            channel.pipeline().fireUserEventTriggered(createHandshakeCompleteEvent());
            return castAny(endpoint.agent());
        }
    }

    @CanIgnoreReturnValue
    protected <T> @NotNull T setupAgent(@NotNull Class<T> klass,
                                        @NotNull Marshal marshal,
                                        @NotNull FrameType frameType,
                                        @NotNull FrameMetadata metadata,
                                        @NotNull ClientInfo clientInfo) {
        WebsocketSetup<T> setup = testSetup(klass, settings -> {
            settings.setDefaultFrameContentMarshal(marshal);
            settings.setDefaultFrameType(frameType);
        }, TestingModules.instance(FrameMetadata.class, metadata));
        return setup.initAgent(clientInfo);
    }

    protected @NotNull Queue<WebSocketFrame> sendText(@NotNull String text) {
        return send(new TextWebSocketFrame(text));
    }

    protected @NotNull Queue<WebSocketFrame> sendBinary(@NotNull String binaryText) {
        return sendBinary(TestingBytes.asBytes(binaryText));
    }

    protected @NotNull Queue<WebSocketFrame> sendBinary(byte @NotNull [] bytes) {
        return send(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
    }

    protected @NotNull Queue<WebSocketFrame> send(@NotNull WebSocketFrame frame) {
        assertChannelInitialized();
        channel.writeOneInbound(frame);
        return readAllFramesUntilEmpty();
    }

    protected @NotNull Queue<WebSocketFrame> readAllPendingFrames() {
        flushChannel();
        return readAllOutbound(channel);
    }

    protected @NotNull Queue<WebSocketFrame> readAllFramesUntilEmpty() {
        Queue<WebSocketFrame> queue = new ArrayDeque<>();
        while (true) {
            Queue<WebSocketFrame> frames = readAllPendingFrames();
            queue.addAll(frames);
            if (frames.isEmpty()) {
                break;
            }
        }
        return queue;
    }

    // A bit of dirty hacking to fire this event.
    private static @NotNull HandshakeComplete createHandshakeCompleteEvent() {
        Class<HandshakeComplete> klass = HandshakeComplete.class;
        Constructor<HandshakeComplete> constructor = castAny(klass.getDeclaredConstructors()[0]);
        constructor.setAccessible(true);
        try {
            return constructor.newInstance("", new DefaultHttpHeaders(), null);
        } catch (Exception e) {
            return Unchecked.rethrow(e);
        }
    }
}
