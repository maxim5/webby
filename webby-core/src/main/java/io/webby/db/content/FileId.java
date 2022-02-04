package io.webby.db.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public record FileId(@NotNull String path) {
    public static @NotNull FileId from(@NotNull ContentId contentId, @NotNull FileFormat format, @NotNull FileExt ext) {
        return from(null, contentId, format, ext);
    }

    public static @NotNull FileId from(@Nullable String dir,
                                       @NotNull ContentId contentId,
                                       @NotNull FileFormat format,
                                       @NotNull FileExt ext) {
        return dir != null && !dir.isEmpty() ?
            new FileId("%s/%s.%s%s".formatted(dir, contentId.contentId(), format.form(), ext.extension())) :
            new FileId("%s.%s%s".formatted(contentId.contentId(), format.form(), ext.extension()));
    }

    public boolean isSafe() {
        Path path = Path.of(this.path);
        return !path.isAbsolute() && path.normalize().equals(path);
    }
}
