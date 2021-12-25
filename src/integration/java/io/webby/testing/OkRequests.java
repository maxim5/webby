package io.webby.testing;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

public record OkRequests(@NotNull String httpUrl, @NotNull String websocketUrl) {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static @NotNull OkRequests of(@NotNull String host) {
        return new OkRequests("http://%s".formatted(host), "ws://%s".formatted(host));
    }

    public static @NotNull OkRequests ofLocalhost(int port) {
        return of("localhost:%d".formatted(port));
    }

    public @NotNull Request get(@NotNull String path) {
        return new Request.Builder().url(httpUrl + path).build();
    }

    public @NotNull Request postJson(@NotNull String path, @NotNull String body) {
        return new Request.Builder().url(httpUrl + path).method("POST", RequestBody.create(body, JSON)).build();
    }

    public @NotNull Request websocket(@NotNull String path) {
        return new Request.Builder().url(websocketUrl + path).build();
    }
}
