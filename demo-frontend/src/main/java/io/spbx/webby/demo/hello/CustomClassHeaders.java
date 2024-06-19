package io.spbx.webby.demo.hello;

import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Header;
import io.spbx.webby.url.annotate.Http;

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
