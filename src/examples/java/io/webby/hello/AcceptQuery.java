package io.webby.hello;

import io.webby.netty.request.HttpRequestEx;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Serve;
import org.jetbrains.annotations.NotNull;

@Serve
public class AcceptQuery {
    @GET(url="/request/query/simple")
    public String simple(@NotNull HttpRequestEx request) {
        return "[%s] [%s]".formatted(request.path(), request.query());
    }

    @GET(url="/request/query/param/{key}")
    public String param_simple(@NotNull HttpRequestEx request, String key) {
        return "[%s] <%s>".formatted(request.params().getOrNull(key), request.params().getAll(key));
    }
}
