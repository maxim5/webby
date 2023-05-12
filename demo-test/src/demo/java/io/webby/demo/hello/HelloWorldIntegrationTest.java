package io.webby.demo.hello;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.HttpRequestBuilder;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assertThat;

public class HelloWorldIntegrationTest extends BaseHttpIntegrationTest {
    protected final HelloWorld handler = testSetup(HelloWorld.class).initHandler();

    @Test
    public void get_hello() {
        assertThat(get("/")).is200().hasContent("Hello World!");
        assertThat(get("//")).is404();

        assertThat(get("/int/10")).is200().hasContent("Hello int <b>10</b>!");
        assertThat(get("/int/777")).is200().hasContent("Hello int <b>777</b>!");
        assertThat(get("/int/0")).is400();
        assertThat(get("/int/-1")).is400();
        assertThat(get("/int/foo")).is400();
        assertThat(get("/int/")).is404();
        assertThat(get("/int/10/")).is404();

        assertThat(get("/int/10/20")).is200().hasContent("Hello int/int x=<b>10</b> y=<b>20</b>!");
        assertThat(get("/int/foo/20")).is400();
        assertThat(get("/int/10/bar")).is400();
        assertThat(get("/int/10/20/")).is404();

        assertThat(get("/intstr/foo/10")).is200().hasContent("Hello int/str <b>foo</b> and <b>10</b>!");
        assertThat(get("/intstr/foo/0")).is200().hasContent("Hello int/str <b>foo</b> and <b>0</b>!");
        assertThat(get("/intstr/foo/-5")).is200().hasContent("Hello int/str <b>foo</b> and <b>-5</b>!");
    }

    @Test
    public void failed_to_parse_request() {
        FullHttpRequest request = HttpRequestBuilder.get("/").withHeader("content-length", "(content-length, 3)").full();
        request.setDecoderResult(DecoderResult.failure(new IllegalArgumentException("Multiple Content-Length values found")));
        assertThat(call(request)).is400();
    }
}
