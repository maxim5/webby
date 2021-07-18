package io.webby.hello;

import io.webby.url.POST;
import io.webby.url.Serve;
import io.webby.url.validate.IntValidator;
import io.webby.url.validate.Validator;

import java.util.HashMap;

@Serve
public class AcceptContent {
    public static final Validator param_id = IntValidator.POSITIVE;

    @POST(url="/int/{id}")
    public Object update_integer(int id) {
        return new HashMap<>();
    }

    @POST(url="/intstr/{str}/{y}")
    public String update_str_int(CharSequence str, int y, Object content) {
        return "Vars: str=%s y=%d content=<%s>".formatted(str, y, content.toString());
    }
}
