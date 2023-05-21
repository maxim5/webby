package io.webby.demo.model;

import io.webby.auth.session.DefaultSession;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.annotate.ManyToMany;
import org.jetbrains.annotations.NotNull;

@ManyToMany
public record BridgeLongModel(@NotNull ForeignLong<DefaultSession> foo,
                              @NotNull ForeignLong<DefaultSession> bar) {
}
