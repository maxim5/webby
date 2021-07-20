package io.webby.hello;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Serve;
import io.webby.url.convert.Converter;
import io.webby.url.convert.StringConverter;
import org.jetbrains.annotations.NotNull;

@Serve
public class AcceptRequest {
    private static final Converter<String> param_name = StringConverter.DEFAULT_256;
    final Converter<String> param_buf = new StringConverter(10);

    @GET(url="/str/{name}")
    public String string(@NotNull HttpRequest request, String name) {
        return "Hello str <b>%s</b> from <b>%s</b>!".formatted(name, request.uri());
    }

    @GET(url="/buf/{buf}")
    public String buffer(@NotNull FullHttpRequest request, CharBuffer buf) {
        return "Hello buf <b>%s</b> from <i>%s</i>!".formatted(buf, request.uri());
    }

    @GET(url="/buf/{buf}/{str}")
    public String buffer(@NotNull FullHttpRequest request, CharBuffer buf1, CharBuffer buf2) {
        return "Hello buf/buf <b>%s</b> and <b>%s</b> from <i>%s</i>!".formatted(buf1, buf2, request.uri());
    }
}
