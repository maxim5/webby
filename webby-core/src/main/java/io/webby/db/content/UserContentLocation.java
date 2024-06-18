package io.webby.db.content;

import io.spbx.util.base.OneOf;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

public class UserContentLocation {
    private final OneOf<Path, URI> location;

    public UserContentLocation(@NotNull OneOf<Path, URI> location) {
        this.location = location;
    }

    public boolean isLocal() {
        return location.hasFirst();
    }

    public @NotNull Path getLocalPath() {
        return requireNonNull(location.first());
    }

    public boolean isRemote() {
        return location.hasSecond();
    }

    public @NotNull URI getRemoteUrl() {
        return requireNonNull(location.second());
    }

    @Override
    public String toString() {
        return "UserContentLocation{%s}".formatted(location);
    }
}
