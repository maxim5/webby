package io.webby.demo.hello;

import io.spbx.webby.netty.request.HttpRequestEx;
import io.spbx.webby.url.annotate.Access;
import io.spbx.webby.url.annotate.GET;
import org.jetbrains.annotations.NotNull;

@Access(Access.AuthUsersOnly)
public class RequireAuth {
    @GET(url = "/access/auth_only")
    public String authOnly(@NotNull HttpRequestEx request) {
        return "ok:%s".formatted(request.user());
    }

    @GET(url = "/access/admin_only")
    @Access(Access.SuperAdminOnly)
    public String adminOnly(@NotNull HttpRequestEx request) {
        return "ok:%s".formatted(request.user());
    }

    @GET(url = "/access/public")
    @Access(Access.Public)
    public String forAll(@NotNull HttpRequestEx request) {
        return "ok:%s".formatted(request.user());
    }
}
