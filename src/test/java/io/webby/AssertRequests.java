package io.webby;

import com.google.common.truth.Truth;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import static io.webby.FakeRequests.asByteBuf;

public class AssertRequests {
    public static void assert200(FullHttpResponse response) {
        assert200(response, null);
    }

    public static void assert200(FullHttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.OK, content);
    }

    public static void assert400(FullHttpResponse response) {
        assert400(response, null);
    }

    public static void assert400(FullHttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.BAD_REQUEST, content);
    }

    public static void assert404(FullHttpResponse response) {
        assert404(response, null);
    }

    public static void assert404(FullHttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.NOT_FOUND, content);
    }

    public static void assert500(FullHttpResponse response) {
        assertResponse(response, HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
    }

    public static void assert503(FullHttpResponse response) {
        assertResponse(response, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
    }

    public static void assertResponse(FullHttpResponse response, HttpResponseStatus status, String content) {
        assertResponse(response, HttpVersion.HTTP_1_1, status, content, null);
    }

    public static void assertResponse(FullHttpResponse response,
                                         HttpVersion version,
                                         HttpResponseStatus status,
                                         @Nullable String content,
                                         @Nullable HttpHeaders headers) {
        Truth.assertThat(response).isNotNull();
        ByteBuf byteBuf = content != null ? asByteBuf(content) : response.content();
        HttpHeaders httpHeaders = (headers != null) ? headers : response.headers();
        HttpHeaders trailingHeaders = response.trailingHeaders();
        Truth.assertThat(response)
                .isEqualTo(new DefaultFullHttpResponse(version, status, byteBuf, httpHeaders, trailingHeaders));
    }

    public static void assertHeaders(FullHttpResponse response, CharSequence key, CharSequence value) {
        Assertions.assertEquals(value.toString(), response.headers().get(key));
    }

    public static void assertHeaders(FullHttpResponse response,
                                        CharSequence key1, CharSequence value1,
                                        CharSequence key2, CharSequence value2) {
        Assertions.assertEquals(value1.toString(), response.headers().get(key1));
        Assertions.assertEquals(value2.toString(), response.headers().get(key2));
    }

    public static void assertContentContains(FullHttpResponse response, String ... substrings) {
        String content = response.content().toString();
        for (String string : substrings) {
            Truth.assertThat(content).contains(string);
        }
    }
}
