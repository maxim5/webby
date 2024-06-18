package io.webby.demo.model;

import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.annotate.ManyToMany;
import io.webby.auth.user.DefaultUser;
import org.jetbrains.annotations.NotNull;

@ManyToMany
public record BridgeIntModel(@NotNull ForeignInt<DefaultUser> foo,
                             @NotNull ForeignInt<DefaultUser> bar) {
}
