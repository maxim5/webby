package io.spbx.webby.netty.request;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.netty.channel.embedded.EmbeddedChannel;
import io.spbx.webby.testing.Testing;
import io.spbx.webby.testing.TestingModules;
import io.spbx.webby.testing.netty.intercept.FakeInterceptorScanner;
import io.spbx.webby.url.impl.EndpointContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.webby.testing.HttpRequestBuilder.post;

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

        assertThat(request1.contentAsJson(Foo.class).fooBar).isNull();
        assertThat(request2.contentAsJson(Foo.class).fooBar).isEqualTo("bar");
    }

    @Test
    public void createRequest_get_json_content_custom_gson() {
        Gson gson = new Gson().newBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
        HttpRequestFactory factory = getFactoryInstance(TestingModules.instance(Gson.class, gson));

        Object content1 = Map.of("foo-bar", "bar");
        Object content2 = Map.of("fooBar", "bar");
        DefaultHttpRequestEx request1 = factory.createRequest(post("/a").withContent(content1).full(), channel, context);
        DefaultHttpRequestEx request2 = factory.createRequest(post("/a").withContent(content2).full(), channel, context);

        assertThat(request1.contentAsJson(Foo.class).fooBar).isEqualTo("bar");
        assertThat(request2.contentAsJson(Foo.class).fooBar).isNull();
    }

    private static @NotNull HttpRequestFactory getFactoryInstance(@NotNull Module @NotNull ... modules) {
        Module module = Modules.override(FakeInterceptorScanner.FAKE_SCANNER.asGuiceModule()).with(modules);
        return Testing.testStartup(module).getInstance(HttpRequestFactory.class);
    }

    static class Foo {
        String fooBar;
    }
}
