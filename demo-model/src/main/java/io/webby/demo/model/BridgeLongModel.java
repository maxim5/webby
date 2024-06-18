package io.webby.demo.model;

import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.annotate.ManyToMany;
import io.spbx.webby.auth.session.DefaultSession;
import org.jetbrains.annotations.NotNull;

@ManyToMany
public record BridgeLongModel(@NotNull ForeignLong<DefaultSession> foo,
                              @NotNull ForeignLong<DefaultSession> bar) {
}
