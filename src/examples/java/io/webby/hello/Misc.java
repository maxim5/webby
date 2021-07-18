package io.webby.hello;

import io.netty.handler.codec.http.HttpRequest;
import io.webby.url.GET;
import io.webby.url.Serve;
import org.jetbrains.annotations.NotNull;

@Serve
public class Misc {
    @GET(url="/misc/void")
    public void misc_void(@NotNull HttpRequest request) {
    }

    @GET(url="/misc/null")
    public Object misc_null() {
        return null;
    }

    @GET(url="/error/npe")
    public Object error_npe() {
        throw new NullPointerException("Message");
    }
}
