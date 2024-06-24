package io.spbx.webby.common;

import io.spbx.util.io.LiveConfig;
import io.spbx.util.io.LiveConfig.IntProperty;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class SystemProperties {
    public static final IntProperty SIZE_BYTES = new IntProperty("webby.byte.stream.size", 1024);
    public static final IntProperty SIZE_CHARS = new IntProperty("webby.char.stream.size", 1024);
    public static final IntProperty SQL_MAX_PARAMS = new IntProperty("webby.sql.max.params", 1024);

    private static final LiveConfig live = LiveConfig.idle(Path.of("app.properties"));

    @CheckReturnValue
    public static @NotNull LiveConfig live() {
        return live;
    }
}
