package io.webby;

import io.routekit.util.CharBuffer;
import io.webby.url.GET;
import io.webby.url.Serve;
import io.webby.url.validate.IntValidator;
import io.webby.url.validate.StringValidator;
import io.webby.url.validate.Validator;

@Serve
public class HelloWorld {
    public static final Validator param_id = IntValidator.POSITIVE;
    private static final Validator param_name = StringValidator.DEFAULT_256;
    Validator param_buf = new StringValidator(10);

    @GET(url="/")
    public String home() {
        return "Hello World!";
    }

    @GET(url="/int/{id}")
    public String integer(int id) {
        return "Hello int <b>%d</b>!".formatted(id);
    }

    @GET(url="/str/{name}")
    public String string(String name) {
        return "Hello str <b>%s</b>!".formatted(name);
    }

    @GET(url="/buf/{buf}")
    public String buffer(CharBuffer buf) {
        return "Hello buf <b>%s</b>!".formatted(buf);
    }
}
