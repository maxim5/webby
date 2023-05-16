package io.webby.demo.model;

import com.google.inject.Inject;
import io.webby.auth.session.SessionModel;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.auth.user.UserModel;
import io.webby.auth.user.UserStore;
import io.webby.netty.errors.NotFoundException;
import io.webby.netty.request.HttpRequestEx;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.POST;
import io.webby.url.annotate.Serve;
import org.jetbrains.annotations.NotNull;

@Serve
public class UserHandler {
    @Inject private UserStore users;

    @GET(url = "/user/{id}")
    public UserModel get(int userId) {
        UserModel user = users.getUserByIdOrNull(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    @GET(url = "/user/")
    public String getAll() {
        return "all users";
    }

    @POST(url = "/user/")
    public Object create(@NotNull HttpRequestEx request) {
        UserModel user = request.user();
        SessionModel session = request.session();
        if (!session.hasUser()) {
            UserModel newUser = users.createUserAutoId(DefaultUser.newAuto(UserAccess.Simple));
            return newUser.userId();
        } else {
            System.out.println(users.getUserByIdOrNull(session.user()));
            return "already exists: " + user;
        }
    }
}
