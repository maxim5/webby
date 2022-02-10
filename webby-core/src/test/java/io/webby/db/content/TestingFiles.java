package io.webby.db.content;

import org.jetbrains.annotations.NotNull;

public class TestingFiles {
    public static @NotNull FileExt ext(@NotNull String extension) {
        return new FileExt(extension);
    }

    public static @NotNull FileFormat format(@NotNull String form) {
        return new FileFormat(form);
    }

    public static @NotNull ContentId contentId(@NotNull String id) {
        return new ContentId(id);
    }
}
