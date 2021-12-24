package io.webby.netty.response;

import com.google.inject.Inject;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class StaticServing {
    @Inject private Settings settings;
    @Inject private HttpCachingRequestProcessor cachingProcessor;

    public void iterateStaticFiles(@NotNull Consumer<String> consumer) throws IOException {
        Path webPath = settings.webPath();
        Files.walk(webPath).forEach(path -> {
            if (path.toFile().isFile()) {
                consumer.accept(webPath.relativize(path).toString());
            }
        });
    }

    public boolean accept(@NotNull HttpMethod method) {
        return method.equals(HttpMethod.GET);
    }

    public @NotNull HttpResponse serve(@NotNull String path, @NotNull FullHttpRequest request) throws IOException {
        Path fullPath = settings.webPath().resolve(path);
        return cachingProcessor.process(fullPath, request);
    }
}
