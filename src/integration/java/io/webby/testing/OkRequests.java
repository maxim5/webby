package io.webby.testing;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

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

    public @NotNull Request post(@NotNull String path, @NotNull RequestBody body) {
        return new Request.Builder().url(httpUrl + path).method("POST", body).build();
    }

    public static @NotNull RequestBody json(@NotNull String body) {
        return RequestBody.create(body, JSON);
    }

    public static @NotNull RequestBody files(@NotNull String... files) {
        return files(Arrays.stream(files).map(File::new).toArray(File[]::new));
    }

    public static @NotNull RequestBody files(@NotNull File ... files) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (File file : files) {
            builder.addFormDataPart(file.getName(), file.getName(),
                                    RequestBody.create(file, MediaType.parse("application/octet-stream")));
        }
        return builder.build();
    }

    public static @NotNull RequestBody files(@NotNull Map<String, byte[]> files) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getKey(), RequestBody.create(entry.getValue()));
        }
        return builder.build();
    }

    public static @NotNull RequestBody multipart(@NotNull Map<String, String> parts) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (Map.Entry<String, String> entry : parts.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public @NotNull Request websocket(@NotNull String path) {
        return new Request.Builder().url(websocketUrl + path).build();
    }
}
