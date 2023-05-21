package io.webby.testing;

import com.google.inject.Injector;
import com.google.inject.Module;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.webby.app.AppSettings;
import io.webby.netty.dispatch.http.NettyHttpHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.function.Consumer;

public abstract class BaseHttpIntegrationTest extends BaseChannelTest {
    protected <T> @NotNull HttpSetup<T> testSetup(@NotNull Class<T> klass) {
        return testSetup(klass, settings -> {});
    }

    protected <T> @NotNull HttpSetup<T> testSetup(@NotNull Class<T> klass,
                                                  @NotNull Consumer<AppSettings> consumer,
                                                  @NotNull Module... modules) {
        Injector injector = Testing.testStartup(
            DEFAULT_SETTINGS
                        .andThen(settings -> settings.handlerFilter().setSingleClassOnly(klass))
                        .andThen(consumer),
            // Make sure the initialized handler will be the same instance in tests as in endpoints
            TestingBasics.appendVarArg(TestingModules.singleton(klass), modules)
        );
        return new HttpSetup<>(injector, klass);
    }

    protected class HttpSetup<T> extends SingleTargetSetup<T> {
        public HttpSetup(@NotNull Injector injector, @NotNull Class<T> klass) {
            super(injector, klass);
            NettyHttpHandler handler = injector.getInstance(NettyHttpHandler.class);
            channel = new EmbeddedChannel(new ChunkedWriteHandler(), handler);
        }

        public @NotNull T initHandler() {
            return injector.getInstance(klass);
        }
    }

    protected @NotNull HttpResponse get(@NotNull String uri) {
        return call(HttpMethod.GET, uri, null);
    }

    protected @NotNull HttpResponse post(@NotNull String uri) {
        return post(uri, null);
    }

    protected @NotNull HttpResponse post(@NotNull String uri, @Nullable Object content) {
        return call(HttpMethod.POST, uri, content);
    }

    protected @NotNull HttpResponse put(@NotNull String uri) {
        return call(HttpMethod.PUT, uri, null);
    }

    protected @NotNull HttpResponse delete(@NotNull String uri) {
        return call(HttpMethod.DELETE, uri, null);
    }

    protected @NotNull HttpResponse head(@NotNull String uri) {
        return call(HttpMethod.HEAD, uri, null);
    }

    protected @NotNull HttpResponse options(@NotNull String uri) {
        return call(HttpMethod.OPTIONS, uri, null);
    }

    protected @NotNull HttpResponse patch(@NotNull String uri) {
        return call(HttpMethod.PATCH, uri, null);
    }

    protected @NotNull HttpResponse call(@NotNull HttpMethod method, @NotNull String uri, @Nullable Object content) {
        return call(HttpRequestBuilder.request(method, uri).withContent(content).full());
    }

    protected @NotNull HttpResponse call(@NotNull FullHttpRequest request) {
        assertChannelInitialized();
        recreateChannelIfClosed();

        channel.writeOneInbound(request);
        flushChannel();

        Queue<HttpObject> outbound = readAllOutbound(channel);
        return outbound.size() == 1 ?
                (HttpResponse) outbound.poll() :
                CompositeHttpResponse.fromObjects(outbound);
    }

    // Will not be necessary if it's possible to close streaming without closing the channel.
    private void recreateChannelIfClosed() {
        if (!channel.isOpen()) {
            NettyHttpHandler handler = injector.getInstance(NettyHttpHandler.class);
            channel = new EmbeddedChannel(new ChunkedWriteHandler(), handler);
        }
    }
}
