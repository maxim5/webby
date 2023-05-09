package io.webby.netty.intercept;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.errors.NotFoundException;
import io.webby.netty.errors.ServeException;
import io.webby.netty.intercept.testing.FakeInterceptorScanner;
import io.webby.netty.intercept.testing.MockingInterceptor;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.request.MutableHttpRequestEx;
import io.webby.netty.response.EmptyHttpResponse;
import io.webby.testing.FakeEndpoints;
import io.webby.testing.Testing;
import io.webby.testing.TestingModules;
import io.webby.url.impl.Endpoint;
import io.webby.url.impl.EndpointContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.webby.testing.AssertResponse.assert404;
import static io.webby.testing.HttpRequestBuilder.get;
import static io.webby.testing.HttpRequestBuilder.post;
import static org.junit.jupiter.api.Assertions.*;

public class InterceptorsTest {
    private final EmbeddedChannel channel = new EmbeddedChannel();
    private final Endpoint endpoint = FakeEndpoints.fakeEndpoint();
    private final EndpointContext context = endpoint.context();
    private final EmptyHttpResponse defaultResponse = EmptyHttpResponse.INSTANCE;

    @Test
    public void lifecycle_simple() {
        MockingInterceptor mockingInterceptor = new MockingInterceptor();
        Interceptors interceptors = getInterceptorsInstance(FakeInterceptorScanner.of(mockingInterceptor));
        DefaultHttpRequestEx request = interceptors.createRequest(get("/foo").full(), channel, context);

        HttpResponse response = interceptors.enter(request, endpoint);
        assertNull(response);
        mockingInterceptor.assertEvents().containsExactly("0:enter");

        interceptors.exit(request, defaultResponse);
        mockingInterceptor.assertEvents().containsExactly("0:enter", "0:exit");

        interceptors.cleanup();
        mockingInterceptor.assertEvents().containsExactly("0:enter", "0:exit", "0:cleanup");
    }

    @Test
    public void lifecycle_interceptors_called_in_order() {
        MockingInterceptor.Factory factory = new MockingInterceptor.Factory();
        Interceptors interceptors = getInterceptorsInstance(FakeInterceptorScanner.of(
            factory.spawn(1),
            factory.spawn(2)
        ));
        DefaultHttpRequestEx request = interceptors.createRequest(get("/foo").full(), channel, context);

        HttpResponse response = interceptors.enter(request, endpoint);
        assertNull(response);
        factory.assertEvents().containsExactly("1:enter", "2:enter");

        interceptors.exit(request, defaultResponse);
        factory.assertEvents().containsExactly("1:enter", "2:enter", "2:exit", "1:exit");

        interceptors.cleanup();
        factory.assertEvents().containsExactly("1:enter", "2:enter", "2:exit", "1:exit", "2:cleanup", "1:cleanup");
    }

    @Test
    public void lifecycle_interceptors_serve_error() {
        MockingInterceptor.Factory factory = new MockingInterceptor.Factory();
        Interceptors interceptors = getInterceptorsInstance(FakeInterceptorScanner.of(
            factory.spawn(111),
            factory.share(new MockingInterceptor() {
                @Override
                public void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
                    super.enter(request);
                    throw new NotFoundException("Not Found");
                }
            }),
            factory.spawn(222)
        ));
        DefaultHttpRequestEx request = interceptors.createRequest(get("/foo").full(), channel, context);

        HttpResponse response = interceptors.enter(request, endpoint);
        assertNotNull(response);
        assert404(response);
        factory.assertEvents().containsExactly("111:enter", "0:enter");

        interceptors.cleanup();
        factory.assertEvents().containsExactly("111:enter", "0:enter", "222:cleanup", "0:cleanup", "111:cleanup");
    }

    @Test
    public void createRequest_get_json_content_standard_gson() {
        Interceptors interceptors = getInterceptorsInstance();

        Object content1 = Map.of("foo-bar", "bar");
        Object content2 = Map.of("fooBar", "bar");
        DefaultHttpRequestEx request1 = interceptors.createRequest(post("/foo").withContent(content1).full(), channel, context);
        DefaultHttpRequestEx request2 = interceptors.createRequest(post("/foo").withContent(content2).full(), channel, context);

        assertNull(request1.contentAsJson(Foo.class).fooBar);
        assertEquals("bar", request2.contentAsJson(Foo.class).fooBar);
    }

    @Test
    public void createRequest_get_json_content_custom_gson() {
        Gson gson = new Gson().newBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
        Interceptors interceptors = getInterceptorsInstance(TestingModules.instance(Gson.class, gson));

        Object content1 = Map.of("foo-bar", "bar");
        Object content2 = Map.of("fooBar", "bar");
        DefaultHttpRequestEx request1 = interceptors.createRequest(post("/foo").withContent(content1).full(), channel, context);
        DefaultHttpRequestEx request2 = interceptors.createRequest(post("/foo").withContent(content2).full(), channel, context);

        assertEquals("bar", request1.contentAsJson(Foo.class).fooBar);
        assertNull(request2.contentAsJson(Foo.class).fooBar);
    }

    private static @NotNull Interceptors getInterceptorsInstance(@NotNull Module @NotNull ... modules) {
        return getInterceptorsInstance(FakeInterceptorScanner.FAKE_SCANNER, modules);
    }

    private static @NotNull Interceptors getInterceptorsInstance(@NotNull FakeInterceptorScanner fakeScanner,
                                                                 @NotNull Module @NotNull ... modules) {
        Module module = Modules.override(TestingModules.instance(InterceptorScanner.class, fakeScanner)).with(modules);
        return Testing.testStartup(module).getInstance(Interceptors.class);
    }

    static class Foo {
        String fooBar;
    }
}
