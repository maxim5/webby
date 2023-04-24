package io.webby.demo.model;

import io.webby.auth.session.Session;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.annotate.ManyToMany;
import org.jetbrains.annotations.NotNull;

@ManyToMany
public record BridgeLongModel(@NotNull ForeignLong<Session> foo,
                              @NotNull ForeignLong<Session> bar) {
}
