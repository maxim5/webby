package io.webby.db.content;

import org.jetbrains.annotations.NotNull;

public record FileId(@NotNull String path) {
    public static @NotNull FileId from(@NotNull ContentId contentId, @NotNull FileSize size, @NotNull FileExt ext) {
        return new FileId("%s.%s%s".formatted(contentId.contentId(), size.size(), ext.extension()));
    }
}
