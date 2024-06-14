package io.webby.testing;

import com.google.common.collect.Streams;
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
import io.webby.perf.stats.Stat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.webby.testing.AssertJson.assertJsonValue;
import static io.webby.testing.TestingBytes.*;
import static io.webby.util.base.EasyCast.castAny;
import static io.webby.util.base.Unchecked.Consumers;
import static io.webby.util.base.Unchecked.Suppliers;
import static org.junit.jupiter.api.Assertions.fail;

public class AssertResponse {
    public static final String TEXT_HTML_CHARSET = "text/html; charset=%s".formatted(Testing.Internals.charset());
    // See https://stackoverflow.com/questions/13827325/correct-mime-type-for-favicon-ico
    public static final List<CharSequence> ICON_MIME_TYPES = List.of("image/x-icon", "image/x-ico", "image/vnd.microsoft.icon");
    public static final List<CharSequence> JS_MIME_TYPES = List.of("application/javascript", "text/javascript");
    public static final List<CharSequence> MIDI_MIME_TYPES = List.of("audio/midi", "audio/mid");

    @CheckReturnValue
    public static @NotNull HttpResponseSubject<HttpResponseSubject<?>> assertThat(@Nullable HttpResponse response) {
        return Truth.assertAbout(metadata -> new CustomSubjectBuilder(metadata) {
            public @NotNull HttpResponseSubject<HttpResponseSubject<?>> that(@Nullable HttpResponse response) {
                return new HttpResponseSubject<>(metadata(), response);
            }
        }).that(response);
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
            assertBytes(content()).isEqualTo(content);
            return castAny(this);
        }

        public @NotNull S hasContent(@NotNull ByteBuf content) {
            assertBytes(content()).isEqualTo(content);
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

        public @NotNull S hasJsonContent(@NotNull Object object) {
            assertJsonValue(content(), object);
            return castAny(this);
        }

        public @NotNull S matchesContent(@NotNull HttpResponse response) {
            return hasContent(AssertResponse.contentOf(response));
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

        public @NotNull S hasNoStatsHeaders() {
            Truth.assertThat(response().headers().get(HttpConst.SERVER_TIMING)).isNull();
            return castAny(this);
        }

        public @NotNull S hasStatsHeader(@NotNull Stat... expectedStats) {
            return hasStatsHeader(Arrays.stream(expectedStats).toList());
        }

        public @NotNull S hasStatsHeader(@NotNull Iterable<Stat> expectedStats) {
            String serverTiming = response().headers().get(HttpConst.SERVER_TIMING);
            Truth.assertThat(serverTiming).isNotNull();

            Matcher matcher = Pattern.compile("main;desc=\"\\{(.*)}\"").matcher(serverTiming);
            Truth.assertThat(matcher.matches()).isTrue();

            Set<String> keys = Arrays.stream(matcher.group(1).split(","))
                .map(part -> part.split(":")[0])
                .collect(Collectors.toSet());
            Set<String> expected = Stream.concat(
                Streams.stream(expectedStats).map(stat -> stat.name().toLowerCase()),
                Stream.of("time")
            ).collect(Collectors.toSet());
            Truth.assertThat(keys).containsExactlyElementsIn(expected);

            return castAny(this);
        }

        public @NotNull S hasStatsHeaderWhichMatches(@NotNull String pattern) {
            String serverTiming = response().headers().get(HttpConst.SERVER_TIMING);
            Truth.assertThat(serverTiming).matches(pattern);
            return castAny(this);
        }

        protected @NotNull HttpResponse response() {
            Truth.assertThat(response).isNotNull();
            return response;
        }

        protected @NotNull ByteBuf content() {
            return AssertResponse.contentOf(response());
        }
    }

    public static @NotNull ByteBuf contentOf(@NotNull HttpResponse response) {
        if (response instanceof ByteBufHolder full) {
            return full.content();
        }
        if (response instanceof DefaultHttpResponse) {
            return Unpooled.EMPTY_BUFFER;
        }
        return fail("Unrecognized response: " + response);
    }

    public static @NotNull ByteBuf streamContentOf(@NotNull HttpResponse response) {
        if (response instanceof StreamingHttpResponse streaming) {
            return Suppliers.rethrow(() -> Unpooled.wrappedBuffer(readAllFrom(streaming))).get();
        }
        return contentOf(response);
    }

    public static byte @NotNull [] readAllFrom(@NotNull StreamingHttpResponse streaming) throws Exception {
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

    public static byte @NotNull [] readAllFrom(@NotNull Stream<HttpContent> stream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.forEach(Consumers.rethrow(content ->
            content.content().readBytes(outputStream, content.content().readableBytes())
        ));
        return outputStream.toByteArray();
    }
}
