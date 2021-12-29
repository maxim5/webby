package io.webby.examples.hello;

import io.webby.url.annotate.GET;
import io.webby.url.annotate.Header;
import io.webby.url.annotate.Http;

@Http(
    headers = {
        @Header(name="Cache-Control", value = "no-cache")
    }
)
public class CustomClassHeaders {
    @GET(url="/headers/etag")
    @Http(headers = @Header(name = "Etag", value = "foobar"))
    public String etag() {
        return "etag";
    }

    @GET(url="/headers/cache")
    @Http(headers = @Header(name = "cache-control", value = "only-if-cached"))
    public String cache() {
        return "cache";
    }
}
