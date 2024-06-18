package io.webby.demo.hello;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.spbx.util.base.CharArray;
import io.spbx.webby.netty.request.DefaultHttpRequestEx;
import io.spbx.webby.netty.request.HttpRequestEx;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Serve;
import org.jetbrains.annotations.NotNull;

@Serve
public class AcceptRequest {
    @GET(url="/request/simple")
    public String simple(@NotNull HttpRequest request) {
        return "Hi <b>%s</b>!".formatted(request.uri());
    }

    @GET(url="/request/one_string/{name}")
    public String one_string(@NotNull DefaultHttpRequest request, String name) {
        return "Hi str <b>%s</b> from <b>%s</b>!".formatted(name, request.uri());
    }

    @GET(url="/request/one_array/{buf}")
    public String one_array(@NotNull FullHttpRequest request, CharArray buf) {
        return "Hi buffer <b>%s</b> from <i>%s</i>!".formatted(buf, request.uri());
    }

    @GET(url="/request/two_arrays/{buf}/{str}")
    public String two_arrays(@NotNull DefaultFullHttpRequest request, CharArray buf1, CharArray buf2) {
        return "Hi buffer <b>%s</b> and buffer <b>%s</b> from <i>%s</i>!".formatted(buf1, buf2, request.uri());
    }

    @GET(url="/request/one_int/{val}")
    public String one_int(HttpRequestEx request, int val) {
        return "Hi %d from <b>%s</b>!".formatted(val, request.path());
    }

    @GET(url="/request/two_ints/{val}/{other}")
    public String two_ints(DefaultHttpRequestEx request, int val, int other) {
        return "Hi %d + %d from <b>%s</b>!".formatted(val, other, request.path());
    }
}
