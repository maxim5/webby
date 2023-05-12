package io.webby.testing;

import com.google.common.truth.CustomSubjectBuilder;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.webby.netty.HttpConst;
import io.webby.netty.response.StreamingHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

import static io.webby.testing.TestingBytes.*;
import static io.webby.util.base.EasyCast.castAny;
import static io.webby.util.base.Unchecked.Consumers;
import static io.webby.util.base.Unchecked.Suppliers;
import static org.junit.jupiter.api.Assertions.fail;

public class AssertResponse {
    public static final String TEXT_HTML_CHARSET = "text/html; charset=%s".formatted(Testing.Internals.charset());
    // See https://stackoverflow.com/questions/13827325/correct-mime-type-for-favicon-ico
    public static final List<CharSequence> ICON_MIME_TYPES = List.of("image/x-icon", "image/x-ico", "image/vnd.microsoft.icon");

    @CheckReturnValue
    public static @NotNull HttpResponseSubject<HttpResponseSubject<?>> assertThat(@Nullable HttpResponse response) {
        return Truth.assertAbout(HttpResponseSubjectBuilder::new).that(response);
    }

    @CanIgnoreReturnValue
    public static class HttpResponseSubject<S extends HttpResponseSubject<?>> extends Subject {
        private final HttpResponse response;

        public HttpResponseSubject(@NotNull FailureMetadata metadata, @Nullable HttpResponse response) {
            super(metadata, response);
            this.response = response;
        }

        public @NotNull S hasStatus(@NotNull HttpResponseStatus status) {
            Truth.assertThat(response().status()).isEqualTo(status);
            return castAny(this);
        }

        public @NotNull S is200() {
            return hasStatus(HttpResponseStatus.OK);
        }

        public @NotNull S is400() {
            return hasStatus(HttpResponseStatus.BAD_REQUEST);
        }

        public @NotNull S is401() {
            return hasStatus(HttpResponseStatus.UNAUTHORIZED);
        }

        public @NotNull S is403() {
            return hasStatus(HttpResponseStatus.FORBIDDEN);
        }

        public @NotNull S is404() {
            return hasStatus(HttpResponseStatus.NOT_FOUND);
        }

        public @NotNull S is500() {
            return hasStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

        public @NotNull S is503() {
            return hasStatus(HttpResponseStatus.SERVICE_UNAVAILABLE);
        }

        public @NotNull S isTempRedirect(@NotNull String url) {
            hasStatus(HttpResponseStatus.TEMPORARY_REDIRECT).hasHeader(HttpConst.LOCATION, url);
            return castAny(this);
        }

        public @NotNull S isPermRedirect(@NotNull String url) {
            hasStatus(HttpResponseStatus.PERMANENT_REDIRECT).hasHeader(HttpConst.LOCATION, url);
            return castAny(this);
        }

        public @NotNull S hasContent(@NotNull String content) {
            assertByteBuf(content(), content);
            return castAny(this);
        }

        public @NotNull S hasContent(@NotNull ByteBuf content) {
            assertByteBufs(content(), content);
            return castAny(this);
        }

        public @NotNull S hasContentWhichContains(@NotNull String @NotNull ... substrings) {
            String content = content().toString(CHARSET);
            for (String string : substrings) {
                Truth.assertThat(content).contains(string);
            }
            return castAny(this);
        }

        public @NotNull S hasContentIgnoringNewlines(@NotNull String expected) {
            Truth.assertThat(asLines(content())).containsExactlyElementsIn(expected.lines().toList());
            return castAny(this);
        }

        public @NotNull S matchesContent(@NotNull HttpResponse response) {
            return hasContent(AssertResponse.content(response));
        }

        public @NotNull S hasEmptyContent() {
            return hasContent("");
        }

        public @NotNull S hasHeadersExactly(@NotNull HttpHeaders headers) {
            Truth.assertThat(response().headers()).isEqualTo(headers);
            return castAny(this);
        }

        public @NotNull S hasHeader(@NotNull CharSequence key, @NotNull CharSequence value) {
            Truth.assertThat(response().headers().get(key)).isEqualTo(value.toString());
            return castAny(this);
        }

        public @NotNull S hasContentType(@NotNull CharSequence contentType) {
            return hasHeader(HttpConst.CONTENT_TYPE, contentType);
        }

        public @NotNull S hasContentLength(int length) {
            return hasHeader(HttpConst.CONTENT_LENGTH, String.valueOf(length));
        }

        protected @NotNull HttpResponse response() {
            Truth.assertThat(response).isNotNull();
            return response;
        }

        protected @NotNull ByteBuf content() {
            return AssertResponse.content(response());
        }
    }

    private static class HttpResponseSubjectBuilder extends CustomSubjectBuilder {
        protected HttpResponseSubjectBuilder(FailureMetadata metadata) {
            super(metadata);
        }

        public @NotNull HttpResponseSubject<HttpResponseSubject<?>> that(@Nullable HttpResponse response) {
            return new HttpResponseSubject<>(metadata(), response);
        }
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
