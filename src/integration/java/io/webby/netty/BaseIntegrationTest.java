package io.webby.netty;

import com.google.inject.Injector;
import io.netty.channel.local.LocalChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.Testing;
import io.webby.app.AppSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static io.webby.AssertResponse.readable;
import static io.webby.FakeRequests.request;

public abstract class BaseIntegrationTest {
    protected NettyChannelHandler handler;

    protected void testStartup(@NotNull Class<?> clazz) {
        testStartup(clazz, __ -> {});
    }

    protected void testStartup(@NotNull Class<?> clazz, @NotNull Consumer<AppSettings> consumer) {
        Injector injector = Testing.testStartup(settings -> {
            settings.setWebPath("src/examples/resources/web");
            settings.setViewPath("src/examples/resources/web");
            settings.setHandlerClassOnly(clazz);
            consumer.accept(settings);
        });
        handler = injector.getInstance(NettyChannelHandler.class);
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
        FullHttpRequest request = request(method, uri, content);
        HttpResponse response = handler.withChannel(new LocalChannel()).handle(request);
        return Testing.READABLE ? readable(response) : response;
    }
}
