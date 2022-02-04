package io.webby.db.content;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.NotNull;

public record FileExt(@NotNull String extension) {
    public static final FileExt EMPTY = new FileExt("");

    public static @NotNull FileExt fromName(@NotNull String name, boolean forceLower) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < name.length() - 1) {
            String extension = name.substring(lastDot);
            return new FileExt(forceLower ? extension.toLowerCase() : extension);
        }
        return EMPTY;
    }

    public static @NotNull FileExt fromUrl(@NotNull String url, boolean forceLower) {
        return FileExt.fromName(new QueryStringDecoder(url).rawPath(), forceLower);  // FIX[perf]: can be more efficient
    }
}
