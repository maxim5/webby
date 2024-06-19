package io.spbx.webby.common;

import org.jetbrains.annotations.NotNull;

public class SystemProperties {
    public static final int DEFAULT_SIZE_BYTES = getInt("webby.byte.stream.size", 1024);
    public static final int DEFAULT_SIZE_CHARS = getInt("webby.char.stream.size", 1024);

    public static final int DEFAULT_SQL_MAX_PARAMS = getInt("webby.sql.max.params", 1024);

    public static int getInt(@NotNull String name, int def) {
        String property = System.getProperty(name);
        if (property != null) {
            try {
                return Integer.parseInt(property);
            } catch (NumberFormatException ignore) {
            }
        }
        return def;
    }
}
