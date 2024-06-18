package io.spbx.webby.demo.model;

import com.google.inject.Inject;
import io.spbx.webby.auth.session.SessionModel;
import io.spbx.webby.auth.user.DefaultUser;
import io.spbx.webby.auth.user.UserAccess;
import io.spbx.webby.auth.user.UserModel;
import io.spbx.webby.auth.user.UserStore;
import io.spbx.webby.netty.errors.NotFoundException;
import io.spbx.webby.netty.request.HttpRequestEx;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.POST;
import io.spbx.webby.url.annotate.Serve;
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
        if (!session.isAuthenticated()) {
            UserModel newUser = users.createUserAutoId(DefaultUser.newUserData(UserAccess.Simple));
            return newUser.userId();
        } else {
            System.out.println(users.getUserByIdOrNull(session.user()));
            return "already exists: " + user;
        }
    }
}
