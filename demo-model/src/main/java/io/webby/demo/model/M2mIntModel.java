package io.webby.demo.model;

import io.webby.auth.user.DefaultUser;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.annotate.ManyToMany;
import org.jetbrains.annotations.NotNull;

@ManyToMany
public record M2mIntModel(@NotNull ForeignInt<DefaultUser> foo,
                          @NotNull ForeignInt<DefaultUser> bar) {
}
