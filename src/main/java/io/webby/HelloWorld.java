package io.webby;

import io.routekit.util.CharBuffer;
import io.webby.url.GET;
import io.webby.url.Serve;

@Serve
public class HelloWorld {
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

    @GET(url="/buf/{name}")
    public String buffer(CharBuffer name) {
        return "Hello buf <b>%s</b>!".formatted(name);
    }
}
