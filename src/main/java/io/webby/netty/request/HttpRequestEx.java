package io.webby.netty.request;

import com.google.gson.JsonParseException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.webby.auth.session.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.List;

public interface HttpRequestEx extends FullHttpRequest {
    @NotNull Charset charset();

    @NotNull String path();

    @NotNull String query();

    @NotNull QueryParams params();

    <T> @NotNull T contentAsJson(@NotNull Class<T> klass) throws JsonParseException;

    @Nullable Object attr(int position);

    <T> @NotNull T attrOrDie(int position);

    @NotNull List<Cookie> cookies();

    @NotNull Session session();
}
