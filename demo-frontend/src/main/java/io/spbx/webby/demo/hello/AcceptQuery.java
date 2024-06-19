package io.spbx.webby.demo.hello;

import io.spbx.webby.netty.request.HttpRequestEx;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Serve;
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
