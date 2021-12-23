package io.webby.db.kv;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record KeyValueSettings(@NotNull StorageType type, @NotNull Path path) {
    public static final StorageType DEFAULT_TYPE = StorageType.JAVA_MAP;
    public static final KeyValueSettings AUTO_DETECT = new KeyValueSettings(DEFAULT_TYPE, Path.of("storage"));
    private static final Path EMPTY_PATH = Path.of("");

    public static @NotNull KeyValueSettings of(@NotNull StorageType type, @NotNull Path path) {
        return new KeyValueSettings(type, path);
    }

    public static @NotNull KeyValueSettings of(@NotNull StorageType type, @NotNull String path) {
        return of(type, Path.of(path));
    }

    public static @NotNull KeyValueSettings of(@NotNull StorageType type) {
        return of(type, EMPTY_PATH);
    }

    public @NotNull KeyValueSettings with(@NotNull StorageType type) {
        return of(type, this.path);
    }
}
