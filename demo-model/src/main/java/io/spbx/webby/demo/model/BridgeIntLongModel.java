package io.spbx.webby.demo.model;

import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.annotate.ManyToMany;
import io.spbx.webby.auth.session.DefaultSession;
import io.spbx.webby.auth.user.DefaultUser;
import org.jetbrains.annotations.NotNull;

@ManyToMany(left = "first", right = "second")
public record BridgeIntLongModel(long id,
                                 @NotNull ForeignInt<DefaultUser> first,
                                 @NotNull ForeignLong<DefaultSession> second,
                                 boolean flag) {
}
