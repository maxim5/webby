package io.spbx.webby.db.kv;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record KeyValueSettings(@NotNull DbType type, @NotNull Path path) {
    public static final DbType DEFAULT_TYPE = DbType.JAVA_MAP;
    public static final KeyValueSettings AUTO_DETECT = new KeyValueSettings(DEFAULT_TYPE, Path.of("storage"));
    private static final Path EMPTY_PATH = Path.of("");

    public static @NotNull KeyValueSettings of(@NotNull DbType type, @NotNull Path path) {
        return new KeyValueSettings(type, path);
    }

    public static @NotNull KeyValueSettings of(@NotNull DbType type, @NotNull String path) {
        return of(type, Path.of(path));
    }

    public static @NotNull KeyValueSettings of(@NotNull DbType type) {
        return of(type, EMPTY_PATH);
    }

    public @NotNull KeyValueSettings with(@NotNull DbType type) {
        return of(type, this.path);
    }
}
