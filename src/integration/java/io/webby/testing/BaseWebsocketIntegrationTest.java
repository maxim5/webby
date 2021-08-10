package io.webby.testing;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.webby.app.AppSettings;
import io.webby.netty.NettyWebsocketHandler;
import io.webby.url.impl.UrlRouter;
import io.webby.url.ws.AgentEndpoint;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.Queue;
import java.util.function.Consumer;

import static io.webby.util.EasyCast.castAny;

public class BaseWebsocketIntegrationTest extends BaseChannelTest {
    protected <T> @NotNull T testStartup(@NotNull Class<T> klass) {
        return testStartup(klass, __ -> {});
    }

    protected <T> @NotNull T testStartup(@NotNull Class<T> klass, @NotNull Consumer<AppSettings> consumer) {
        injector = Testing.testStartup(settings -> {
            settings.setWebPath("src/examples/resources/web");
            settings.setViewPath("src/examples/resources/web");
            settings.setHandlerClassOnly(klass);
            consumer.accept(settings);
        });

        AgentEndpoint endpoint = injector.getInstance(UrlRouter.class).findAgentEndpointByClass(klass);
        Assertions.assertNotNull(endpoint, "No Endpoint found for: %s".formatted(klass));
        NettyWebsocketHandler handler = new NettyWebsocketHandler(endpoint);
        injector.injectMembers(handler);
        channel = new EmbeddedChannel(handler);
        return castAny(endpoint.instance());
    }

    protected Queue<WebSocketFrame> sendText(@NotNull String text) {
        return send(new TextWebSocketFrame(text));
    }

    protected Queue<WebSocketFrame> sendBinary(@NotNull String binaryText) {
        return sendBinary(FakeFrames.bytes(binaryText));
    }

    protected Queue<WebSocketFrame> sendBinary(byte @NotNull [] bytes) {
        return send(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
    }

    protected Queue<WebSocketFrame> send(@NotNull WebSocketFrame frame) {
        assertChannelInitialized();

        channel.writeOneInbound(frame);
        flushChannel();
        Queue<WebSocketFrame> outbound = readAllOutbound(channel);

        return outbound;
    }
}
