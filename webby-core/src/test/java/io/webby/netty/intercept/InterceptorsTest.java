package io.webby.netty.intercept;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.errors.NotFoundException;
import io.webby.netty.errors.ServeException;
import io.webby.netty.intercept.testing.FakeInterceptorScanner;
import io.webby.netty.intercept.testing.MockingInterceptor;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.request.MutableHttpRequestEx;
import io.webby.netty.response.EmptyHttpResponse;
import io.webby.testing.FakeEndpoints;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.Testing;
import io.webby.url.impl.Endpoint;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assert404;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InterceptorsTest {
    private final Endpoint endpoint = FakeEndpoints.fakeEndpoint();
    private final EmptyHttpResponse defaultResponse = EmptyHttpResponse.INSTANCE;

    @Test
    public void lifecycle_simple() {
        MockingInterceptor mockingInterceptor = new MockingInterceptor();
        Interceptors interceptors = getInterceptorsInstance(FakeInterceptorScanner.of(mockingInterceptor));
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();

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
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();

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
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();

        HttpResponse response = interceptors.enter(request, endpoint);
        assertNotNull(response);
        assert404(response);
        factory.assertEvents().containsExactly("111:enter", "0:enter");

        interceptors.cleanup();
        factory.assertEvents().containsExactly("111:enter", "0:enter", "222:cleanup", "0:cleanup", "111:cleanup");
    }

    private static @NotNull Interceptors getInterceptorsInstance(@NotNull FakeInterceptorScanner fakeScanner) {
        return Testing.testStartup(fakeScanner.asGuiceModule()).getInstance(Interceptors.class);
    }
}
