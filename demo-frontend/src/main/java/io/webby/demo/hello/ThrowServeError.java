package io.webby.demo.hello;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.webby.netty.errors.*;
import io.webby.url.annotate.GET;
import org.jetbrains.annotations.NotNull;

public class ThrowServeError {
    @GET(url="/r/error/307")
    public Object throw_temporary_redirect(@NotNull HttpRequest request) {
        throw new RedirectException("/", false);
    }

    @GET(url="/r/error/308")
    public Object throw_permanent_redirect(@NotNull HttpRequest request) {
        throw new RedirectException("/", true);
    }

    @GET(url="/r/error/400")
    public Object throw_bad_request(@NotNull HttpRequest request) {
        throw new BadRequestException("Bad request");
    }

    @GET(url="/r/error/401")
    public Object throw_unauthorized(@NotNull HttpRequest request) {
        throw new UnauthorizedException("Unauthorized");
    }

    @GET(url="/r/error/403")
    public Object throw_forbidden(@NotNull HttpRequest request) {
        throw new ForbiddenException("Forbidden");
    }

    @GET(url="/r/error/404")
    public Object throw_not_found(@NotNull HttpRequest request) {
        throw new NotFoundException("Not found");
    }

    @GET(url="/r/error/503")
    public Object throw_service_unavailable(@NotNull HttpRequest request) {
        throw new ServiceUnavailableException("Unavailable");
    }

    @GET(url="/r/error/custom/{code}")
    public Object throw_custom(int code, @NotNull HttpRequest request) {
        throw new HttpCodeException(HttpResponseStatus.valueOf(code));
    }
}
