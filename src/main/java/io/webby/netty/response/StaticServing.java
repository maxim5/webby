package io.webby.netty.response;

import com.google.inject.Inject;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.Settings;
import io.webby.netty.response.HttpCachingRequestProcessor.ContentProvider;
import io.webby.util.io.EasyFiles;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class StaticServing {
    @Inject private Settings settings;
    @Inject private HttpResponseFactory factory;
    @Inject private ContentTypeDetector detector;

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
        return new HttpCachingRequestProcessor(settings, factory, new ContentProvider() {
            @Override
            public boolean exists() {
                return Files.exists(fullPath);
            }
            @Override
            public long getFileSizeInBytes() throws IOException {
                return Files.size(fullPath);
            }
            @Override
            public long getLastModifiedMillis() throws IOException {
                return EasyFiles.getLastModifiedTime(fullPath);
            }
            @Override
            public CharSequence getContentType() {
                return detector.guessContentType(fullPath);
            }
            @Override
            public InputStream readFileContent() throws IOException {
                return new FileInputStream(fullPath.toFile());
            }
        }).process(path, request);
    }
}
