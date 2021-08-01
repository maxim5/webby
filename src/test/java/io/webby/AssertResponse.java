package io.webby;

import com.google.common.truth.Truth;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import static io.webby.FakeRequests.asByteBuf;

public class AssertResponse {
    public static void assert200(HttpResponse response) {
        assert200(response, null);
    }

    public static void assert200(HttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.OK, content);
    }

    public static void assert400(HttpResponse response) {
        assert400(response, null);
    }

    public static void assert400(HttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.BAD_REQUEST, content);
    }

    public static void assert404(HttpResponse response) {
        assert404(response, null);
    }

    public static void assert404(HttpResponse response, String content) {
        assertResponse(response, HttpResponseStatus.NOT_FOUND, content);
    }

    public static void assert500(HttpResponse response) {
        assertResponse(response, HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
    }

    public static void assert503(HttpResponse response) {
        assertResponse(response, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
    }

    public static void assertResponse(HttpResponse response, HttpResponseStatus status, String content) {
        assertResponse(response, HttpVersion.HTTP_1_1, status, content, null);
    }

    public static void assertResponse(HttpResponse response,
                                      HttpVersion version,
                                      HttpResponseStatus status,
                                      @Nullable String expectedContent,
                                      @Nullable HttpHeaders expectedHeaders) {
        Truth.assertThat(response).isNotNull();
        HttpHeaders headers = (expectedHeaders != null) ? expectedHeaders : response.headers();

        if (response instanceof DefaultFullHttpResponse full) {
            ByteBuf byteBuf = expectedContent != null ? asByteBuf(expectedContent) : full.content();
            HttpHeaders trailing = full.trailingHeaders();
            Truth.assertThat(response).isEqualTo(new DefaultFullHttpResponse(version, status, byteBuf, headers, trailing));
        } else if (response instanceof DefaultHttpResponse) {
            Truth.assertThat(response).isEqualTo(new DefaultHttpResponse(version, status, headers));
        } else {
            Assertions.fail("Unrecognized response: " + response);
        }
    }

    public static void assertHeaders(HttpResponse response, CharSequence key, CharSequence value) {
        Assertions.assertEquals(value.toString(), response.headers().get(key));
    }

    public static void assertHeaders(HttpResponse response,
                                     CharSequence key1, CharSequence value1,
                                     CharSequence key2, CharSequence value2) {
        Assertions.assertEquals(value1.toString(), response.headers().get(key1));
        Assertions.assertEquals(value2.toString(), response.headers().get(key2));
    }

    public static void assertContentContains(HttpResponse response, String ... substrings) {
        String content = fullContent(response).toString();
        for (String string : substrings) {
            Truth.assertThat(content).contains(string);
        }
    }

    @NotNull public static ByteBuf fullContent(@NotNull HttpResponse response) {
        if (response instanceof FullHttpResponse full) {
            return full.content();
        }
        if (response instanceof DefaultHttpResponse) {
            return Unpooled.buffer(0);
        }
        return Assertions.fail("Unrecognized response: " + response);
    }

    @NotNull public static HttpResponse readable(@NotNull HttpResponse response) {
        if (response instanceof FullHttpResponse full) {
            return full.replace(FakeRequests.readable(full.content()));
        }
        return response;
    }
}
