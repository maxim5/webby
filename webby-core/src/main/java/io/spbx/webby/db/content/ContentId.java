package io.spbx.webby.db.content;

import org.jetbrains.annotations.NotNull;

public record ContentId(@NotNull String contentId) {
    public ContentId {
        assert !contentId.isEmpty() : "Content id can't be empty";
    }
}
