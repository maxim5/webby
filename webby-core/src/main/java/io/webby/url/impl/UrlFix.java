package io.webby.url.impl;

import org.jetbrains.annotations.NotNull;

class UrlFix {
    public static @NotNull String joinWithSlash(@NotNull CharSequence... parts) {
        StringBuilder builder = new StringBuilder();
        for (CharSequence part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.isEmpty()) {
                builder.append(part);
            } else {
                int i = builder.length() - 1;
                while (i >= 0 && builder.charAt(i) == '/') {
                    i--;
                }
                builder.setLength(i + 1);
                builder.append('/');

                int j = 0;
                while (j < part.length() && part.charAt(j) == '/') {
                    j++;
                }
                builder.append(part, j, part.length());
            }
        }
        return builder.toString();
    }
}
