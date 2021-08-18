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
import io.webby.netty.NettyHttpHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.function.Consumer;

import static io.webby.testing.FakeRequests.request;

public abstract class BaseHttpIntegrationTest extends BaseChannelTest {
    protected <T> @NotNull HttpSetup<T> testSetup(@NotNull Class<T> klass) {
        return testSetup(klass, settings -> {});
    }

    protected <T> @NotNull HttpSetup<T> testSetup(@NotNull Class<T> klass,
                                                  @NotNull Consumer<AppSettings> consumer,
                                                  @NotNull Module... modules) {
        Injector injector = Testing.testStartup(
                DEFAULT_SETTINGS
                        .andThen(settings -> settings.setHandlerClassOnly(klass))
                        .andThen(consumer),
                modules
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

    @NotNull
    protected HttpResponse get(String uri) {
        return call(HttpMethod.GET, uri, null);
    }

    @NotNull
    protected HttpResponse post(String uri) {
        return post(uri, null);
    }

    @NotNull
    protected HttpResponse post(String uri, @Nullable Object content) {
        return call(HttpMethod.POST, uri, content);
    }

    @NotNull
    protected HttpResponse call(HttpMethod method, String uri, @Nullable Object content) {
        assertChannelInitialized();

        FullHttpRequest request = request(method, uri, content);
        channel.writeOneInbound(request);
        flushChannel();

        Queue<HttpObject> outbound = readAllOutbound(channel);
        return outbound.size() == 1 ?
                (HttpResponse) outbound.poll() :
                CompositeHttpResponse.fromObjects(outbound);
    }
}
