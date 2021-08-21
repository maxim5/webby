package io.webby.testing;

import com.google.common.truth.Truth;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.webby.netty.response.StreamingHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import static io.webby.testing.TestingBytes.asByteBuf;
import static io.webby.util.Rethrow.*;

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

    public static void assertResponse(@Nullable HttpResponse response,
                                      @NotNull HttpVersion version,
                                      @NotNull HttpResponseStatus status,
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

    public static void assertHeaders(@NotNull HttpResponse response, @NotNull CharSequence key, @NotNull CharSequence value) {
        Assertions.assertEquals(value.toString(), response.headers().get(key));
    }

    public static void assertHeaders(@NotNull HttpResponse response,
                                     @NotNull CharSequence key1, @NotNull CharSequence value1,
                                     @NotNull CharSequence key2, @NotNull CharSequence value2) {
        Assertions.assertEquals(value1.toString(), response.headers().get(key1));
        Assertions.assertEquals(value2.toString(), response.headers().get(key2));
    }

    public static void assertContentLength(@NotNull HttpResponse response, @NotNull CharSequence expected) {
        assertHeaders(response, HttpHeaderNames.CONTENT_LENGTH, expected);
    }

    public static void assertContentType(@NotNull HttpResponse response, @NotNull CharSequence expected) {
        assertHeaders(response, HttpHeaderNames.CONTENT_TYPE, expected);
    }

    public static void assertContentContains(HttpResponse response, String ... substrings) {
        String content = content(response).toString(Charset.defaultCharset());
        for (String string : substrings) {
            Truth.assertThat(content).contains(string);
        }
    }

    public static @NotNull ByteBuf content(@NotNull HttpResponse response) {
        if (response instanceof ByteBufHolder full) {
            return full.content();
        }
        if (response instanceof DefaultHttpResponse) {
            return Unpooled.EMPTY_BUFFER;
        }
        return Assertions.fail("Unrecognized response: " + response);
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
