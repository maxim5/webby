package io.webby.examples.hello;

import io.webby.url.annotate.POST;
import io.webby.url.annotate.Param;
import io.webby.url.annotate.Serve;
import io.webby.url.convert.IntConverter;

import java.util.HashMap;

@Serve
public class AcceptContent {
    @Param(var="id") public static final IntConverter paramId = IntConverter.POSITIVE;

    @POST(url="/int/{id}")
    public Object update_integer(int id) {
        return new HashMap<>();
    }

    @POST(url="/intstr/{str}/{y}")
    public String update_str_int(CharSequence str, int y, Object content) {
        return "Vars: str=%s y=%d content=<%s>".formatted(str, y, content.toString());
    }
}