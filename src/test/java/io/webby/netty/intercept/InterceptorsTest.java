package io.webby.netty.intercept;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.inject.Module;
import io.netty.channel.embedded.EmbeddedChannel;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.testing.Testing;
import io.webby.testing.TestingModules;
import io.webby.url.impl.EndpointContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.webby.testing.FakeRequests.post;

public class InterceptorsTest {
    private final Interceptors interceptors = Testing.testStartupNoHandlers().getInstance(Interceptors.class);

    @Test
    public void createRequest_get_json_content_standard_gson() {
        Object content1 = Map.of("foo-bar", "bar");
        Object content2 = Map.of("fooBar", "bar");

        EmbeddedChannel channel = new EmbeddedChannel();
        EndpointContext context = new EndpointContext(Map.of(), false, false);
        DefaultHttpRequestEx request1 = interceptors.createRequest(post("/foo", content1), channel, context);
        Assertions.assertNull(request1.contentAsJson(Foo.class).fooBar);
        DefaultHttpRequestEx request2 = interceptors.createRequest(post("/foo", content2), channel, context);
        Assertions.assertEquals("bar", request2.contentAsJson(Foo.class).fooBar);
    }

    @Test
    public void createRequest_get_json_content_custom_gson() {
        Module module = TestingModules.provided(Gson.class,
                () -> new Gson().newBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create());
        Interceptors interceptors = Testing.testStartupNoHandlers(module).getInstance(Interceptors.class);

        Object content1 = Map.of("foo-bar", "bar");
        Object content2 = Map.of("fooBar", "bar");

        EmbeddedChannel channel = new EmbeddedChannel();
        EndpointContext context = new EndpointContext(Map.of(), false, false);
        DefaultHttpRequestEx request1 = interceptors.createRequest(post("/foo", content1), channel, context);
        Assertions.assertEquals("bar", request1.contentAsJson(Foo.class).fooBar);
        DefaultHttpRequestEx request2 = interceptors.createRequest(post("/foo", content2), channel, context);
        Assertions.assertNull(request2.contentAsJson(Foo.class).fooBar);
    }

    public static class Foo {
        String fooBar;
    }
}
