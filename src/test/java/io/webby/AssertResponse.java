package io.webby;

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
import java.util.stream.Stream;

import static io.webby.FakeRequests.asByteBuf;
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
        String content = content(response).toString();
        for (String string : substrings) {
            Truth.assertThat(content).contains(string);
        }
    }

    @NotNull public static ByteBuf content(@NotNull HttpResponse response) {
        if (response instanceof ByteBufHolder full) {
            return full.content();
        }
        if (response instanceof DefaultHttpResponse) {
            return Unpooled.EMPTY_BUFFER;
        }
        return Assertions.fail("Unrecognized response: " + response);
    }

    @NotNull public static ByteBuf streamContent(@NotNull HttpResponse response) {
        if (response instanceof StreamingHttpResponse streaming) {
            return Suppliers.rethrow(() -> Unpooled.wrappedBuffer(readAll(streaming))).get();
        }
        return content(response);
    }

    public static byte[] readAll(@NotNull StreamingHttpResponse streaming) throws Exception {
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

    public static byte[] readAll(@NotNull Stream<HttpContent> stream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.forEach(Consumers.rethrow(content ->
                content.content().readBytes(outputStream, content.content().readableBytes())
        ));
        return outputStream.toByteArray();
    }

    @NotNull public static HttpResponse readable(@NotNull HttpResponse response) {
        if (response instanceof FullHttpResponse full) {
            return full.replace(FakeRequests.readable(full.content()));
        }
        return response;
    }
}
