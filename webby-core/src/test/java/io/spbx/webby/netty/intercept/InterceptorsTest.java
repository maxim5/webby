package io.spbx.webby.netty.intercept;

import io.netty.handler.codec.http.HttpResponse;
import io.spbx.webby.netty.errors.NotFoundException;
import io.spbx.webby.netty.request.DefaultHttpRequestEx;
import io.spbx.webby.netty.response.EmptyHttpResponse;
import io.spbx.webby.testing.FakeEndpoints;
import io.spbx.webby.testing.HttpRequestBuilder;
import io.spbx.webby.testing.Testing;
import io.spbx.webby.testing.netty.intercept.FakeInterceptorScanner;
import io.spbx.webby.testing.netty.intercept.MockingInterceptor;
import io.spbx.webby.url.impl.Endpoint;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.spbx.webby.testing.AssertResponse.assertThat;

@Tag("slow")
public class InterceptorsTest {
    private final DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();
    private final Endpoint endpoint = FakeEndpoints.fakeEndpoint();
    private final EmptyHttpResponse defaultResponse = EmptyHttpResponse.INSTANCE;
    private final MockingInterceptor.Factory factory = new MockingInterceptor.Factory();

    @Test
    public void enter_called_in_order() {
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.spawn(222));
        HttpResponse response = interceptors.enter(request, endpoint);
        assertThat(response).isNull();
        factory.assertEvents().containsExactly("111:enter", "222:enter");
    }

    @Test
    public void exit_called_in_reverse_order() {
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.spawn(222));
        interceptors.exit(request, defaultResponse);
        factory.assertEvents().containsExactly("222:exit", "111:exit");
    }

    @Test
    public void cleanup_called_in_reverse_order() {
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.spawn(222));
        interceptors.cleanup();
        factory.assertEvents().containsExactly("222:cleanup", "111:cleanup");
    }

    @Test
    public void process_all_successful() {
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.spawn(222));
        HttpResponse response = interceptors.process(request, endpoint, () -> defaultResponse);
        assertThat(response).isSameInstanceAs(defaultResponse);
        factory.assertEvents().containsExactly("111:enter", "222:enter", "222:exit", "111:exit");
    }

    @Test
    public void process_enter_serve_error() {
        MockingInterceptor failing = new MockingInterceptor(0).forceEnterFail(new NotFoundException("Not Found"));
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.share(failing), factory.spawn(222));

        HttpResponse response = interceptors.process(request, endpoint, () -> defaultResponse);
        assertThat(response).is404();
        factory.assertEvents().containsExactly("111:enter", "0:enter:FAIL",
                                               "222:cleanup", "0:cleanup", "111:cleanup");
    }

    @Test
    public void process_enter_internal_error() {
        MockingInterceptor failing = new MockingInterceptor(0).forceEnterFail(new AssertionError("Oops"));
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.share(failing), factory.spawn(222));

        HttpResponse response = interceptors.process(request, endpoint, () -> defaultResponse);
        assertThat(response).is500();
        factory.assertEvents().containsExactly("111:enter", "0:enter:FAIL",
                                               "222:cleanup", "0:cleanup", "111:cleanup");
    }

    @Test
    public void process_exit_internal_error() {
        MockingInterceptor failing = new MockingInterceptor(0).forceExitFail(new AssertionError("Oops"));
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.share(failing), factory.spawn(222));

        HttpResponse response = interceptors.process(request, endpoint, () -> defaultResponse);
        assertThat(response).is500();
        factory.assertEvents().containsExactly("111:enter", "0:enter", "222:enter",
                                               "222:exit", "0:exit:FAIL",
                                               "222:cleanup", "0:cleanup", "111:cleanup");
    }

    @Test
    public void process_cleanup_internal_error() {
        MockingInterceptor failing = new MockingInterceptor(0).forceCleanupFail(new AssertionError("Oops"));
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.share(failing), factory.spawn(222));

        HttpResponse response = interceptors.process(request, endpoint, () -> defaultResponse);
        assertThat(response).isSameInstanceAs(defaultResponse);
        factory.assertEvents().containsExactly("111:enter", "0:enter", "222:enter",
                                               "222:exit", "0:exit", "111:exit");
    }

    @Test
    public void process_enter_serve_error_and_cleanup_internal_error() {
        MockingInterceptor failing = new MockingInterceptor(0)
            .forceEnterFail(new NotFoundException("Not Found"))
            .forceCleanupFail(new AssertionError("Oops"));
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.share(failing), factory.spawn(222));

        HttpResponse response = interceptors.process(request, endpoint, () -> defaultResponse);
        assertThat(response).is404();
        factory.assertEvents().containsExactly("111:enter", "0:enter:FAIL",
                                               "222:cleanup", "0:cleanup:FAIL", "111:cleanup");
    }

    @Test
    public void process_enter_internal_error_and_cleanup_internal_error() {
        MockingInterceptor failing = new MockingInterceptor(0)
            .forceEnterFail(new AssertionError("Oops"))
            .forceCleanupFail(new AssertionError("Oops"));
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.share(failing), factory.spawn(222));

        HttpResponse response = interceptors.process(request, endpoint, () -> defaultResponse);
        assertThat(response).is500();
        factory.assertEvents().containsExactly("111:enter", "0:enter:FAIL",
                                               "222:cleanup", "0:cleanup:FAIL", "111:cleanup");
    }

    @Test
    public void process_exit_internal_error_cleanup_internal_error() {
        MockingInterceptor failing = new MockingInterceptor(0)
            .forceExitFail(new AssertionError("Oops"))
            .forceCleanupFail(new AssertionError("Oops"));
        Interceptors interceptors = getInterceptors(factory.spawn(111), factory.share(failing), factory.spawn(222));

        HttpResponse response = interceptors.process(request, endpoint, () -> defaultResponse);
        assertThat(response).is500();
        factory.assertEvents().containsExactly("111:enter", "0:enter", "222:enter",
                                               "222:exit", "0:exit:FAIL",
                                               "222:cleanup", "0:cleanup:FAIL", "111:cleanup");
    }

    private static @NotNull Interceptors getInterceptors(@NotNull Interceptor @NotNull ... interceptors) {
        FakeInterceptorScanner fakeScanner = FakeInterceptorScanner.of(interceptors);
        return Testing.testStartup(fakeScanner.asGuiceModule()).getInstance(Interceptors.class);
    }
}
