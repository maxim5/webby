package io.webby.demo.hello;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.HttpRequestBuilder;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class HelloWorldIntegrationTest extends BaseHttpIntegrationTest {
    protected final HelloWorld handler = testSetup(HelloWorld.class).initHandler();

    @Test
    public void get_hello() {
        assert200(get("/"), "Hello World!");
        assert404(get("//"));

        assert200(get("/int/10"), "Hello int <b>10</b>!");
        assert200(get("/int/777"), "Hello int <b>777</b>!");
        assert400(get("/int/0"));
        assert400(get("/int/-1"));
        assert400(get("/int/foo"));
        assert404(get("/int/"));
        assert404(get("/int/10/"));

        assert200(get("/int/10/20"), "Hello int/int x=<b>10</b> y=<b>20</b>!");
        assert400(get("/int/foo/20"));
        assert400(get("/int/10/bar"));
        assert404(get("/int/10/20/"));

        assert200(get("/intstr/foo/10"), "Hello int/str <b>foo</b> and <b>10</b>!");
        assert200(get("/intstr/foo/0"), "Hello int/str <b>foo</b> and <b>0</b>!");
        assert200(get("/intstr/foo/-5"), "Hello int/str <b>foo</b> and <b>-5</b>!");
    }

    @Test
    public void failed_to_parse_request() {
        FullHttpRequest request = HttpRequestBuilder.get("/").withHeader("content-length", "(content-length, 3)").full();
        request.setDecoderResult(DecoderResult.failure(new IllegalArgumentException("Multiple Content-Length values found")));
        assert400(call(request));
    }
}
