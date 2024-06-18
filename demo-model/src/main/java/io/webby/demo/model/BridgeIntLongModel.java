package io.webby.demo.model;

import io.webby.auth.session.DefaultSession;
import io.webby.auth.user.DefaultUser;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.annotate.ManyToMany;
import org.jetbrains.annotations.NotNull;

@ManyToMany(left = "first", right = "second")
public record BridgeIntLongModel(long id,
                                 @NotNull ForeignInt<DefaultUser> first,
                                 @NotNull ForeignLong<DefaultSession> second,
                                 boolean flag) {
}
