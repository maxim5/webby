package io.webby.testing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.webby.netty.HttpConst;
import io.webby.netty.response.StreamingHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingBytes.*;
import static io.webby.util.base.Rethrow.Consumers;
import static io.webby.util.base.Rethrow.Suppliers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class AssertResponse {
    public static final AsciiString APPLICATION_JSON = HttpConst.APPLICATION_JSON;
    public static final String TEXT_HTML_CHARSET = "text/html; charset=%s".formatted(Testing.Internals.charset());
    // See https://stackoverflow.com/questions/13827325/correct-mime-type-for-favicon-ico
    public static final List<CharSequence> ICON_MIME_TYPES = List.of("image/x-icon", "image/x-ico", "image/vnd.microsoft.icon");

    public static void assert200(@NotNull HttpResponse response) {
        assert200(response, null);
    }

    public static void assert200(@NotNull HttpResponse response, @Nullable String content) {
        assertResponse(response, HttpResponseStatus.OK, content);
    }

    public static void assertTempRedirect(@NotNull HttpResponse response, @NotNull String url) {
        assertResponse(response, HttpResponseStatus.TEMPORARY_REDIRECT, null);
        assertHeaders(response, HttpConst.LOCATION, url);
    }

    public static void assertPermRedirect(@NotNull HttpResponse response, @NotNull String url) {
        assertResponse(response, HttpResponseStatus.PERMANENT_REDIRECT, null);
        assertHeaders(response, HttpConst.LOCATION, url);
    }

    public static void assert400(@NotNull HttpResponse response) {
        assert400(response, null);
    }

    public static void assert400(@NotNull HttpResponse response, @Nullable String content) {
        assertResponse(response, HttpResponseStatus.BAD_REQUEST, content);
    }

    public static void assert401(@NotNull HttpResponse response) {
        assertResponse(response, HttpResponseStatus.UNAUTHORIZED, null);
    }

    public static void assert403(@NotNull HttpResponse response) {
        assertResponse(response, HttpResponseStatus.FORBIDDEN, null);
    }

    public static void assert404(@NotNull HttpResponse response) {
        assert404(response, null);
    }

    public static void assert404(@NotNull HttpResponse response, @Nullable String content) {
        assertResponse(response, HttpResponseStatus.NOT_FOUND, content);
    }

    public static void assert500(@NotNull HttpResponse response) {
        assertResponse(response, HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
    }

    public static void assert503(@NotNull HttpResponse response) {
        assertResponse(response, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
    }

    public static void assertResponse(@NotNull HttpResponse response,
                                      @NotNull HttpResponseStatus status,
                                      @Nullable String content) {
        assertResponse(response, HttpVersion.HTTP_1_1, status, content, null);
    }

    public static void assertResponse(@Nullable HttpResponse response,
                                      @NotNull HttpVersion version,
                                      @NotNull HttpResponseStatus status,
                                      @Nullable String expectedContent,
                                      @Nullable HttpHeaders expectedHeaders) {
        assertThat(response).isNotNull();
        HttpHeaders headers = (expectedHeaders != null) ? expectedHeaders : response.headers();

        if (response instanceof DefaultFullHttpResponse full) {
            ByteBuf byteBuf = expectedContent != null ? asByteBuf(expectedContent) : full.content();
            HttpHeaders trailing = full.trailingHeaders();
            assertThat(response).isEqualTo(new DefaultFullHttpResponse(version, status, byteBuf, headers, trailing));
        } else if (response instanceof DefaultHttpResponse) {
            assertThat(response).isEqualTo(new DefaultHttpResponse(version, status, headers));
        } else {
            fail("Unrecognized response: " + response);
        }
    }

    public static void assertHeaders(@NotNull HttpResponse response, @NotNull HttpResponse expected) {
        assertHeaders(response.headers(), expected.headers());
    }

    public static void assertHeaders(@NotNull HttpHeaders headers, @NotNull HttpHeaders expected) {
        assertEquals(expected, headers);
    }

    public static void assertHeaders(@NotNull HttpResponse response,
                                     @NotNull CharSequence key,
                                     @NotNull CharSequence value) {
        assertEquals(value.toString(), response.headers().get(key));
    }

    public static void assertHeaders(@NotNull HttpResponse response,
                                     @NotNull CharSequence key1, @NotNull CharSequence value1,
                                     @NotNull CharSequence key2, @NotNull CharSequence value2) {
        assertEquals(value1.toString(), response.headers().get(key1));
        assertEquals(value2.toString(), response.headers().get(key2));
    }

    public static void assertContentLength(@NotNull HttpResponse response, int expected) {
        assertContentLength(response, String.valueOf(expected));
    }

    public static void assertContentLength(@NotNull HttpResponse response, @NotNull CharSequence expected) {
        assertHeaders(response, HttpConst.CONTENT_LENGTH, expected);
    }

    public static void assertContentType(@NotNull HttpResponse response, @NotNull CharSequence expected) {
        assertHeaders(response, HttpConst.CONTENT_TYPE, expected);
    }

    public static void assertContent(@NotNull HttpResponse response, @NotNull HttpResponse expected) {
        assertByteBufs(content(expected), content(response));
    }

    public static void assertContentContains(@NotNull HttpResponse response, @NotNull String @NotNull ... substrings) {
        String content = content(response).toString(CHARSET);
        for (String string : substrings) {
            assertThat(content).contains(string);
        }
    }

    public static @NotNull HttpHeaders headersWithoutVolatile(@NotNull HttpResponse response) {
        return headersWithoutVolatile(response.headers());
    }

    public static @NotNull HttpHeaders headersWithoutVolatile(@NotNull HttpHeaders headers) {
        return headers.copy()
                .remove(HttpConst.SET_COOKIE)
                .remove(HttpConst.SERVER_TIMING);
    }

    public static @NotNull ByteBuf content(@NotNull HttpResponse response) {
        if (response instanceof ByteBufHolder full) {
            return full.content();
        }
        if (response instanceof DefaultHttpResponse) {
            return Unpooled.EMPTY_BUFFER;
        }
        return fail("Unrecognized response: " + response);
    }

    public static @NotNull ByteBuf streamContent(@NotNull HttpResponse response) {
        if (response instanceof StreamingHttpResponse streaming) {
            return Suppliers.rethrow(() -> Unpooled.wrappedBuffer(readAll(streaming))).get();
        }
        return content(response);
    }

    public static byte @NotNull [] readAll(@NotNull StreamingHttpResponse streaming) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpChunkedInput chunkedInput = streaming.chunkedContent();
        try {
            while (!chunkedInput.isEndOfInput()) {
                HttpContent content = chunkedInput.readChunk(ByteBufAllocator.DEFAULT);
                content.content().readBytes(outputStream, content.content().readableBytes());
            }
        } finally {
            chunkedInput.close();
        }
        return outputStream.toByteArray();
    }

    public static byte @NotNull [] readAll(@NotNull Stream<HttpContent> stream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.forEach(Consumers.rethrow(content ->
                content.content().readBytes(outputStream, content.content().readableBytes())
        ));
        return outputStream.toByteArray();
    }
}
