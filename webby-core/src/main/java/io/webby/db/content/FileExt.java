package io.webby.db.content;

import org.jetbrains.annotations.NotNull;

public record FileExt(@NotNull String extension) {
    public static final FileExt EMPTY = new FileExt("");
    public static final FileExt IMAGE_PNG = new FileExt(".png");

    public static @NotNull FileExt fromName(@NotNull String name, boolean forceLower) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            String extension = name.substring(lastDot);
            return new FileExt(forceLower ? extension.toLowerCase() : extension);
        }
        return EMPTY;
    }
}
