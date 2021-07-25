package io.webby.netty;

import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.DuplicatedByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.webby.Testing;
import io.webby.hello.AcceptContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseIntegrationTest {
    protected NettyChannelHandler handler;

    protected void testStartup(@NotNull Class<?> clazz) {
        Injector injector = Testing.testStartup(clazz);
        handler = injector.getInstance(NettyChannelHandler.class);
    }

    @NotNull
    protected FullHttpResponse get(String uri) {
        return call(HttpMethod.GET, uri, null);
    }

    @NotNull
    protected FullHttpResponse post(String uri) {
        return post(uri, null);
    }

    @NotNull
    protected FullHttpResponse post(String uri, @Nullable Object content) {
        return call(HttpMethod.POST, uri, content);
    }

    @NotNull
    protected FullHttpResponse call(HttpMethod method, String uri, @Nullable Object content) {
        ByteBuf byteBuf = asByteBuf((content instanceof String str) ? str : toJson(content));
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, byteBuf);
        FullHttpResponse response = handler.handle(request);
        return Testing.READABLE ? readable(response) : response;
    }

    @Nullable
    protected static String toJson(@Nullable Object obj) {
        return new Gson().toJson(obj);
    }

    @NotNull
    protected static ByteBuf asByteBuf(@Nullable String content) {
        return BaseIntegrationTest.readable(content != null ? Unpooled.copiedBuffer(content, CharsetUtil.UTF_8) : Unpooled.buffer(0));
    }

    protected static FullHttpResponse readable(@NotNull FullHttpResponse response) {
        return response.replace(BaseIntegrationTest.readable(response.content()));
    }

    protected static ByteBuf readable(ByteBuf buf) {
        return new ReadableByteBuf(buf);
    }

    protected static void assert200(FullHttpResponse response) {
        assert200(response, null);
    }

    protected static void assert200(FullHttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.OK, content);
    }

    protected static void assert400(FullHttpResponse response) {
        assert400(response, null);
    }

    protected static void assert400(FullHttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.BAD_REQUEST, content);
    }

    protected static void assert404(FullHttpResponse response) {
        assert404(response, null);
    }

    protected static void assert404(FullHttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.NOT_FOUND, content);
    }

    protected static void assert503(FullHttpResponse response) {
        assertResponse(response, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
    }

    protected static void assertResponse(FullHttpResponse response, HttpResponseStatus status, String content) {
        assertResponse(response, HttpVersion.HTTP_1_1, status, content, null);
    }

    protected static void assertResponse(FullHttpResponse response,
                                         HttpVersion version,
                                         HttpResponseStatus status,
                                         @Nullable String content,
                                         @Nullable HttpHeaders headers) {
        ByteBuf byteBuf = content != null ? BaseIntegrationTest.asByteBuf(content) : response.content();
        HttpHeaders httpHeaders = (headers != null) ? headers : response.headers();
        HttpHeaders trailingHeaders = response.trailingHeaders();
        Truth.assertThat(response)
                .isEqualTo(new DefaultFullHttpResponse(version, status, byteBuf, httpHeaders, trailingHeaders));
    }

    protected static void assertContentContains(FullHttpResponse response, String ... substrings) {
        String content = response.content().toString();
        for (String string : substrings) {
            Truth.assertThat(content).contains(string);
        }
    }

    @SuppressWarnings("deprecation")
    protected static class ReadableByteBuf extends DuplicatedByteBuf {
        public ReadableByteBuf(ByteBuf buffer) {
            super(buffer);
        }

        @Override
        public String toString() {
            return unwrap().toString(CharsetUtil.UTF_8);
        }
    }
}