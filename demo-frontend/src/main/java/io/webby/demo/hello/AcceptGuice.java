package io.webby.demo.hello;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.spbx.webby.app.Settings;
import io.spbx.webby.netty.response.HttpResponseFactory;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Json;
import io.spbx.webby.url.annotate.POST;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Map;

public class AcceptGuice {
    @GET(url="/guice/simple")
    public @NotNull Charset simple(@NotNull Settings settings) {
        return settings.charset();
    }

    @GET(url="/guice/simple/2")
    public @NotNull FullHttpResponse simple_two(@NotNull Settings settings, @NotNull HttpResponseFactory factory) {
        return factory.newResponse(settings.charset().toString(), HttpResponseStatus.OK);
    }

    @GET(url="/guice/param/str/{id}")
    public @NotNull String str_param_settings(@NotNull String id, @NotNull Settings settings) {
        return "%s:%s".formatted(id, settings.charset());
    }

    @GET(url="/guice/param/int/{id}")
    public @NotNull String int_param_settings(int id, @NotNull Settings settings) {
        return "%d:%s".formatted(id, settings.charset());
    }

    @GET(url="/guice/settings_request")
    public @NotNull Charset settings_request(@NotNull Settings settings, @NotNull HttpRequest request) {
        return settings.charset();
    }

    @GET(url="/guice/request_settings")
    public @NotNull Charset request_settings(@NotNull HttpRequest request, @NotNull Settings settings) {
        return settings.charset();
    }

    @POST(url="/guice/request_settings_content")
    public @NotNull String request_settings_content(@NotNull HttpRequest request, @NotNull Settings settings,
                                                    @Json Map<String, Object> content) {
        return "%s:%s".formatted(content, settings.charset());
    }
}
