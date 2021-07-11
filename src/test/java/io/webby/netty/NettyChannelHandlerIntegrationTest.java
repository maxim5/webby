package io.webby.netty;

import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.DuplicatedByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.webby.url.UrlModule;
import io.webby.url.impl.UrlRouter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogManager;

public class NettyChannelHandlerIntegrationTest {
    private static final boolean VERBOSE = true;
    private NettyChannelHandler handler;

    @BeforeEach
    void setup() {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);  // reduces the noise

        Injector injector = Guice.createInjector(new UrlModule());
        UrlRouter router = injector.getInstance(UrlRouter.class);
        handler = new NettyChannelHandler(router);
    }

    @Test
    public void get_hello_world_simple_pages() {
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

        assert200(get("/str/10"), "Hello str <b>10</b> from <b>/str/10</b>!");
        assert200(get("/str/foo"), "Hello str <b>foo</b> from <b>/str/foo</b>!");
        assert404(get("/str/foo/"));

        assert200(get("/intstr/foo/10"), "Hello int-str <b>foo</b> and <b>10</b>!");
        assert200(get("/intstr/foo/0"), "Hello int-str <b>foo</b> and <b>0</b>!");
        assert200(get("/intstr/foo/-5"), "Hello int-str <b>foo</b> and <b>-5</b>!");

        assert200(get("/buf/foo"), "Hello buf <b>foo</b> from <i>/buf/foo</i>!");
        assert400(get("/buf/foo-bar-baz"));

        assert200(get("/misc/void"), "");
        assert200(get("/misc/null"), "");
        assert503(get("/error/npe"));
    }

    @Test
    public void post_hello_world_simple_pages() {
        assert404(post("/"));

        assert200(post("/int/10"), "{}");
        assert400(post("/int/0"));
        assert400(post("/int/-1"));
        assert400(post("/int/foo"));

        assert200(post("/intstr/foo/10", new int[] {1, 2, 3}), "Vars: str=foo y=10 content=<[1.0, 2.0, 3.0]>");
    }

    @NotNull
    private FullHttpResponse get(String uri) {
        return call(HttpMethod.GET, uri, null);
    }

    @NotNull
    private FullHttpResponse post(String uri) {
        return post(uri, null);
    }

    @NotNull
    private FullHttpResponse post(String uri, @Nullable Object content) {
        return call(HttpMethod.POST, uri, content);
    }

    @NotNull
    private FullHttpResponse call(HttpMethod method, String uri, @Nullable Object content) {
        ByteBuf byteBuf = asByteBuf((content instanceof String str) ? str : toJson(content));
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, byteBuf);
        FullHttpResponse response = handler.handle(request);
        return VERBOSE ? readable(response) : response;
    }

    private void assert200(FullHttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.OK, content);
    }

    private void assert400(FullHttpResponse response) {
        assert400(response, null);
    }

    private void assert400(FullHttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.BAD_REQUEST, content);
    }

    private void assert404(FullHttpResponse response) {
        assert404(response, null);
    }

    private void assert404(FullHttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.NOT_FOUND, content);
    }

    private void assert503(FullHttpResponse response) {
        assertResponse(response, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
    }

    private void assertResponse(FullHttpResponse response, HttpResponseStatus status, String content) {
        assertResponse(response, HttpVersion.HTTP_1_1, status, content, null);
    }

    private static void assertResponse(FullHttpResponse response,
                                       HttpVersion version,
                                       HttpResponseStatus status,
                                       @Nullable String content,
                                       @Nullable HttpHeaders headers) {
        ByteBuf byteBuf = content != null ? asByteBuf(content) : response.content();
        HttpHeaders httpHeaders = (headers != null) ? headers : response.headers();
        HttpHeaders trailingHeaders = response.trailingHeaders();
        Truth.assertThat(response)
                .isEqualTo(new DefaultFullHttpResponse(version, status, byteBuf, httpHeaders, trailingHeaders));
    }

    @Nullable
    private static String toJson(@Nullable Object obj) {
        return new Gson().toJson(obj);
    }

    @NotNull
    private static ByteBuf asByteBuf(@Nullable String content) {
        return readable(content != null ? Unpooled.copiedBuffer(content, CharsetUtil.UTF_8) : Unpooled.buffer(0));
    }

    private static FullHttpResponse readable(@NotNull FullHttpResponse response) {
        return response.replace(readable(response.content()));
    }

    private static ByteBuf readable(ByteBuf buf) {
        return new ReadableByteBuf(buf);
    }

    @SuppressWarnings("deprecation")
    private static class ReadableByteBuf extends DuplicatedByteBuf {
        public ReadableByteBuf(ByteBuf buffer) {
            super(buffer);
        }

        @Override
        public String toString() {
            return unwrap().toString(CharsetUtil.UTF_8);
        }
    }
}
