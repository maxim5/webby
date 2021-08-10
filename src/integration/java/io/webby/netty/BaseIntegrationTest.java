package io.webby.netty;

import com.google.inject.Injector;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.webby.CompositeHttpResponse;
import io.webby.Testing;
import io.webby.app.AppSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.function.Consumer;

import static io.webby.AssertResponse.readable;
import static io.webby.FakeRequests.request;

public abstract class BaseIntegrationTest extends BaseChannelTest {
    protected @NotNull Injector testStartup(@NotNull Class<?> clazz) {
        return testStartup(clazz, __ -> {});
    }

    protected @NotNull Injector testStartup(@NotNull Class<?> clazz, @NotNull Consumer<AppSettings> consumer) {
        injector = Testing.testStartup(settings -> {
            settings.setWebPath("src/examples/resources/web");
            settings.setViewPath("src/examples/resources/web");
            settings.setHandlerClassOnly(clazz);
            consumer.accept(settings);
        });
        NettyRequestHandler handler = injector.getInstance(NettyRequestHandler.class);
        channel = new EmbeddedChannel(new ChunkedWriteHandler(), handler);
        return injector;
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
        HttpResponse response = outbound.size() == 1 ?
                (HttpResponse) outbound.poll() :
                CompositeHttpResponse.fromObjects(outbound);
        return Testing.READABLE ? readable(response) : response;
    }
}
