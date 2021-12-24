package io.webby.netty.response;

import com.google.inject.Inject;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.Settings;
import io.webby.db.content.FileId;
import io.webby.db.content.UserContentStorage;
import io.webby.netty.response.HttpCachingRequestProcessor.ContentProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class UserContentServing {
    @Inject private Settings settings;
    @Inject private UserContentStorage storage;
    @Inject private HttpResponseFactory factory;

    public boolean accept(@NotNull HttpMethod method) {
        return method.equals(HttpMethod.GET);
    }

    public @NotNull HttpResponse serve(@NotNull String path, @NotNull FullHttpRequest request) throws IOException {
        FileId fileId = new FileId(path);
        return new HttpCachingRequestProcessor(settings, factory, new ContentProvider() {
            @Override
            public boolean exists() {
                return storage.exists(fileId);
            }
            @Override
            public long getFileSizeInBytes() throws IOException {
                return storage.getFileSizeInBytes(fileId);
            }
            @Override
            public long getLastModifiedMillis() throws IOException {
                return storage.getLastModifiedMillis(fileId);
            }
            @Override
            public CharSequence getContentType() {
                return storage.getContentType(fileId);
            }
            @Override
            public InputStream readFileContent() throws IOException {
                return storage.readFileContent(fileId);
            }
        }).process(path, request);
    }
}
