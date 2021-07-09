package io.webby;

import com.google.inject.Inject;
import io.webby.url.GET;
import io.webby.url.Serve;

@Serve
public class HelloWorld {
    // Try to drop
    @Inject
    public HelloWorld() {
    }

    @GET(url="/")
    public String get() {
        return "Hello World!";
    }
}
