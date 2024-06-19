package io.spbx.webby.demo.model;

import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.annotate.ManyToMany;
import io.spbx.webby.auth.user.DefaultUser;
import org.jetbrains.annotations.NotNull;

@ManyToMany
public record BridgeIntModel(@NotNull ForeignInt<DefaultUser> foo,
                             @NotNull ForeignInt<DefaultUser> bar) {
}
