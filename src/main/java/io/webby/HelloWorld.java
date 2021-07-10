package io.webby;

import io.webby.url.GET;
import io.webby.url.Serve;

@Serve
public class HelloWorld {
    @GET(url="/")
    public String get() {
        return "Hello World!";
    }
}
