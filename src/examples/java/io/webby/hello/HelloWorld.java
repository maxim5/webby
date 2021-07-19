package io.webby.hello;

import io.webby.url.annotate.GET;
import io.webby.url.annotate.Serve;
import io.webby.url.validate.IntValidator;
import io.webby.url.validate.Validator;

@Serve
public class HelloWorld {
    public static final Validator param_id = IntValidator.POSITIVE;

    @GET(url="/")
    public String home() {
        return "Hello World!";
    }

    @GET(url="/int/{id}")
    public String integer(int id) {
        return "Hello int <b>%d</b>!".formatted(id);
    }

    @GET(url="/int/{id}/{y}")
    public String integer(int x, int y) {
        return "Hello int/int x=<b>%d</b> y=<b>%d</b>!".formatted(x, y);
    }

    @GET(url="/intstr/{str}/{y}")
    public String string(String str, int y) {
        return "Hello int/str <b>%s</b> and <b>%d</b>!".formatted(str, y);
    }

    @GET(url="/seq/{seq}")
    private String private_char_sequence(CharSequence buf) {
        return "Hello seq <b>%s</b>!".formatted(buf);
    }
}
