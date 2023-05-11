package io.webby.netty.request;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.netty.channel.embedded.EmbeddedChannel;
import io.webby.netty.intercept.testing.FakeInterceptorScanner;
import io.webby.testing.Testing;
import io.webby.testing.TestingModules;
import io.webby.url.impl.EndpointContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.webby.testing.HttpRequestBuilder.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HttpRequestFactoryTest {
    private final EmbeddedChannel channel = new EmbeddedChannel();
    private final EndpointContext context = EndpointContext.EMPTY_CONTEXT;

    @Test
    public void createRequest_get_json_content_standard_gson() {
        HttpRequestFactory factory = getFactoryInstance();

        Object content1 = Map.of("foo-bar", "bar");
        Object content2 = Map.of("fooBar", "bar");
        DefaultHttpRequestEx request1 = factory.createRequest(post("/a").withContent(content1).full(), channel, context);
        DefaultHttpRequestEx request2 = factory.createRequest(post("/a").withContent(content2).full(), channel, context);

        assertNull(request1.contentAsJson(Foo.class).fooBar);
        assertEquals("bar", request2.contentAsJson(Foo.class).fooBar);
    }

    @Test
    public void createRequest_get_json_content_custom_gson() {
        Gson gson = new Gson().newBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
        HttpRequestFactory factory = getFactoryInstance(TestingModules.instance(Gson.class, gson));

        Object content1 = Map.of("foo-bar", "bar");
        Object content2 = Map.of("fooBar", "bar");
        DefaultHttpRequestEx request1 = factory.createRequest(post("/a").withContent(content1).full(), channel, context);
        DefaultHttpRequestEx request2 = factory.createRequest(post("/a").withContent(content2).full(), channel, context);

        assertEquals("bar", request1.contentAsJson(Foo.class).fooBar);
        assertNull(request2.contentAsJson(Foo.class).fooBar);
    }

    private static @NotNull HttpRequestFactory getFactoryInstance(@NotNull Module @NotNull ... modules) {
        Module module = Modules.override(FakeInterceptorScanner.FAKE_SCANNER.asGuiceModule()).with(modules);
        return Testing.testStartup(module).getInstance(HttpRequestFactory.class);
    }

    static class Foo {
        String fooBar;
    }
}
