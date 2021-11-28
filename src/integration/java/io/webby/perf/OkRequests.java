package io.webby.perf;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

public class OkRequests {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String baseUrl;

    public OkRequests(@NotNull String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static @NotNull OkRequests of(@NotNull String baseUrl) {
        return new OkRequests(baseUrl);
    }

    public static @NotNull OkRequests of(int port) {
        return of("http://localhost:%d".formatted(port));
    }

    public @NotNull Request get(@NotNull String path) {
        return new Request.Builder().url(baseUrl + path).build();
    }

    public @NotNull Request postJson(@NotNull String path, @NotNull String body) {
        return new Request.Builder().url(baseUrl + path).method("POST", RequestBody.create(body, JSON)).build();
    }
}
