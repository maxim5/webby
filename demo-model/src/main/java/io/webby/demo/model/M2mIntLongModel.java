package io.webby.demo.model;

import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.annotate.ManyToMany;
import org.jetbrains.annotations.NotNull;

@ManyToMany(left = "first", right = "second")
public record M2mIntLongModel(long id,
                              @NotNull ForeignInt<DefaultUser> first,
                              @NotNull ForeignLong<Session> second,
                              boolean flag) {
}
