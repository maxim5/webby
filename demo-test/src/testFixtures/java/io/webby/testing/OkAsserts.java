package io.webby.testing;

import com.google.common.io.BaseEncoding;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Consumer;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class OkAsserts {
    @CheckReturnValue
    public static @NotNull ResponseSubject assertClient(@NotNull Response response) {
        return new ResponseSubject(response);
    }

    @CanIgnoreReturnValue
    public record ResponseSubject(@NotNull Response response) {
        public @NotNull ResponseSubject is200() {
            return hasCode(200);
        }

        public @NotNull ResponseSubject hasCode(int code) {
            assertThat(response.code()).isEqualTo(code);
            return this;
        }

        public @NotNull ResponseSubject hasBody(@Nullable String content) throws IOException {
            if (content == null) {
                assertThat(response.body()).isNull();
            } else {
                assertThat(response.body()).isNotNull();
                assertThat(response.peekBody(Long.MAX_VALUE).string()).isEqualTo(content);
            }
            return this;
        }

        public @NotNull ResponseSubject hasBody(@Nullable Consumer<String> matcher) throws IOException {
            if (matcher == null) {
                assertThat(response.body()).isNull();
            } else {
                assertThat(response.body()).isNotNull();
                matcher.accept(response.peekBody(Long.MAX_VALUE).string());
            }
            return this;
        }

        public @NotNull ResponseSubject hasBody(long length) throws IOException {
            ResponseBody body = response.peekBody(Long.MAX_VALUE);
            assertThat(body).isNotNull();
            assertThat(body.bytes().length).isEqualTo(length);
            return this;
        }

        public @NotNull ResponseSubject hasHeader(@NotNull String name, @NotNull String... expected) {
            assertThat(response.headers(name)).containsExactlyElementsIn(expected).inOrder();
            return this;
        }
    }

    public static @NotNull String describe(@Nullable Request request) {
        if (request == null) {
            return "<null request>";
        }
        String status = request.toString();
        String headers = request.headers().toString();
        return String.join("\n", status, headers);
    }

    public static @NotNull String describe(@Nullable Response response) {
        if (response == null) {
            return "<null response>";
        }
        try {
            String status = response.toString();
            String headers = response.headers().toString();
            String body = describe(response.peekBody(Long.MAX_VALUE));
            return String.join("\n", status, headers, body);
        } catch (IOException e) {
            return fail(e);
        }
    }

    public static @NotNull String describe(@Nullable ResponseBody body) throws IOException {
        if (body == null) {
            return "<null body>";
        }
        MediaType mediaType = body.contentType();
        boolean isText = mediaType != null && mediaType.type().contains("text");
        String result = isText ? body.string() : BaseEncoding.base16().lowerCase().encode(body.bytes());
        if (result.length() > 100) {
            return "%s...<%d more>".formatted(result.substring(0, 100), result.length() - 100);
        }
        return result;
    }
}
