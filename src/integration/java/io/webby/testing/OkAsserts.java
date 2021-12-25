package io.webby.testing;

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
        ResponseBody body = response.body();
        if (content == null) {
            assertNull(body);
        } else {
            assertNotNull(body);
            assertEquals(content, body.string());
        }
    }

    public static void assertClientBody(@NotNull Response response, long length) throws IOException {
        ResponseBody body = response.body();
        assertNotNull(body);
        assertEquals(length, body.bytes().length);
    }

    public static void assertClientHeader(@NotNull Response response, @NotNull String name, @NotNull String... expected) {
        assertThat(response.headers(name)).containsExactlyElementsIn(expected).inOrder();
    }
}
