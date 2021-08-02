package io.webby.hello;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharArray;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.request.HttpRequestEx;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Param;
import io.webby.url.annotate.Serve;
import io.webby.url.convert.Converter;
import io.webby.url.convert.StringConverter;
import org.jetbrains.annotations.NotNull;

@Serve
public class AcceptRequest {
    @Param private static final Converter<String> name = StringConverter.DEFAULT_256;
    @Param final Converter<String> buf = new StringConverter(10);

    @GET(url="/request/simple")
    public String simple(@NotNull HttpRequest request) {
        return "Hello <b>%s</b>!".formatted(request.uri());
    }

    @GET(url="/request/str/{name}")
    public String string(@NotNull DefaultHttpRequest request, String name) {
        return "Hello str <b>%s</b> from <b>%s</b>!".formatted(name, request.uri());
    }

    @GET(url="/request/buffer/{buf}")
    public String buffer(@NotNull FullHttpRequest request, CharArray buf) {
        return "Hello buffer <b>%s</b> from <i>%s</i>!".formatted(buf, request.uri());
    }

    @GET(url="/request/buffer/{buf}/{str}")
    public String buffer(@NotNull DefaultFullHttpRequest request, CharArray buf1, CharArray buf2) {
        return "Hello buffer <b>%s</b> and buffer <b>%s</b> from <i>%s</i>!".formatted(buf1, buf2, request.uri());
    }

    @GET(url="/request/int/{val}")
    public String integer(HttpRequestEx request, int val) {
        return "Hello %d from <b>%s</b>!".formatted(val, request.path());
    }

    @GET(url="/request/int/{val}/{other}")
    public String integer(DefaultHttpRequestEx request, int val, int other) {
        return "Hello %d + %d from <b>%s</b>!".formatted(val, other, request.path());
    }
}
