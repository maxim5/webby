package io.webby.hello;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharBuffer;
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
    public String string(@NotNull HttpRequest request, String name) {
        return "Hello str <b>%s</b> from <b>%s</b>!".formatted(name, request.uri());
    }

    @GET(url="/request/buffer/{buf}")
    public String buffer(@NotNull FullHttpRequest request, CharBuffer buf) {
        return "Hello buffer <b>%s</b> from <i>%s</i>!".formatted(buf, request.uri());
    }

    @GET(url="/request/buffer/{buf}/{str}")
    public String buffer(@NotNull FullHttpRequest request, CharBuffer buf1, CharBuffer buf2) {
        return "Hello buffer <b>%s</b> and buffer <b>%s</b> from <i>%s</i>!".formatted(buf1, buf2, request.uri());
    }
}
