package io.webby.db.content;

import org.jetbrains.annotations.NotNull;

public record FileExt(@NotNull String extension) {
    public static final FileExt EMPTY = new FileExt("");

    public FileExt {
        assert extension.isEmpty() || extension.lastIndexOf('.') == 0 : "Invalid extension: " + extension;
    }

    public static @NotNull FileExt fromName(@NotNull String name, boolean forceLower) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < name.length() - 1) {
            String extension = name.substring(lastDot);
            return new FileExt(forceLower ? extension.toLowerCase() : extension);
        }
        return EMPTY;
    }

    public static @NotNull FileExt fromUrl(@NotNull String url, boolean forceLower) {
        int question = findPathEndIndex(url);
        int slash = Math.max(url.lastIndexOf('/', question), 0);
        return FileExt.fromName(url.substring(slash, question), forceLower);
    }

    private static int findPathEndIndex(@NotNull String uri) {
        int len = uri.length();
        for (int i = 0; i < len; i++) {
            char c = uri.charAt(i);
            if (c == '?' || c == '#') {
                return i;
            }
        }
        return len;
    }

    public @NotNull String pureExtension() {
        return extension.isEmpty() ? "" : extension.substring(1);
    }
}
