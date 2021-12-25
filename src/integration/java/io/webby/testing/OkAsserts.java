package io.webby.testing;

import com.google.common.io.BaseEncoding;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class OkAsserts {
    public static void assertClientCode(@NotNull Response response, int code) {
        assertEquals(code, response.code());
    }

    public static void assertClientBody(@NotNull Response response, @Nullable String content) throws IOException {
        if (content == null) {
            assertNull(response.body());
        } else {
            assertNotNull(response.body());
            assertEquals(content, response.peekBody(Long.MAX_VALUE).string());
        }
    }

    public static void assertClientBody(@NotNull Response response, long length) throws IOException {
        ResponseBody body = response.peekBody(Long.MAX_VALUE);
        assertNotNull(body);
        assertEquals(length, body.bytes().length);
    }

    public static void assertClientHeader(@NotNull Response response, @NotNull String name, @NotNull String... expected) {
        assertThat(response.headers(name)).containsExactlyElementsIn(expected).inOrder();
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
