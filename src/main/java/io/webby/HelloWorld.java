package io.webby;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.GET;
import io.webby.url.Serve;
import io.webby.url.validate.IntValidator;
import io.webby.url.validate.StringValidator;
import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;

@Serve
public class HelloWorld {
    public static final Validator param_id = IntValidator.POSITIVE;
    private static final Validator param_name = StringValidator.DEFAULT_256;
    final Validator param_buf = new StringValidator(10);

    @GET(url="/")
    public String home() {
        return "Hello World!";
    }

    @GET(url="/int/{id}")
    public String integer(int id) {
        return "Hello int <b>%d</b>!".formatted(id);
    }

    @GET(url="/str/{name}")
    public String string(@NotNull HttpRequest request, String name) {
        return "Hello str <b>%s</b> from <b>%s</b>!".formatted(name, request.uri());
    }

    @GET(url="/buf/{buf}")
    public String buffer(@NotNull FullHttpRequest request, CharBuffer buf) {
        return "Hello buf <b>%s</b> from <i>%s</i>!".formatted(buf, request.uri());
    }
}
