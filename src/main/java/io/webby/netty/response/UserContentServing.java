package io.webby.netty.response;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.db.content.FileId;
import io.webby.db.content.UserContentLocation;
import io.webby.db.content.UserContentStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class UserContentServing implements Serving {
    @Inject private UserContentStorage storage;
    @Inject private HttpResponseFactory factory;
    @Inject private HttpCachingRequestProcessor cachingProcessor;

    @Override
    public boolean accept(@NotNull HttpMethod method) {
        return method.equals(HttpMethod.GET);
    }

    @Override
    public @NotNull HttpResponse serve(@NotNull String path, @NotNull HttpRequest request) throws IOException {
        FileId fileId = new FileId(path);
        UserContentLocation location = storage.getLocation(fileId);
        if (location.isLocal()) {
            return cachingProcessor.process(location.getLocalPath(), request);
        } else if (location.isRemote()) {
            return factory.newResponseRedirect(location.getRemoteUrl().toString(), true);
        } else {
            throw new IllegalStateException("Internal error. Unsupported location: " + location);
        }
    }
}
