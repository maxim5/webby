package io.webby.demo.hello;

import io.spbx.webby.url.annotate.*;

@Serve(url = "/multi/{id}")
public class AcceptMultiHandler {
    @GET
    public String get_int(int id) {
        return "get(%d)".formatted(id);
    }

    @POST
    public String post_int(int id, @Json Object content) {
        return "post(%d:%s)".formatted(id, content);
    }

    @PUT
    public String put_int(int id) {
        return "put(%d)".formatted(id);
    }

    @DELETE
    public String delete_int(int id) {
        return "delete(%d)".formatted(id);
    }

    @Call(methods = {"HEAD", "OPTIONS"})
    public String head_options_int(int id) {
        return "head_options(%d)".formatted(id);
    }
}
