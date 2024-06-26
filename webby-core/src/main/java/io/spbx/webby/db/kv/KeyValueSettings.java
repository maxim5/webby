package io.spbx.webby.db.kv;

import io.spbx.util.props.PropertyMap;
import io.spbx.webby.app.Settings;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record KeyValueSettings(@NotNull DbType type, @NotNull Path path) {
    public static final KeyValueSettings DEFAULTS = new KeyValueSettings(Settings.KV_TYPE.def(), Settings.KV_STORAGE_PATH.def());

    public static @NotNull KeyValueSettings fromProperties(@NotNull PropertyMap properties) {
        DbType dbType = properties.getEnum(Settings.KV_TYPE);
        Path path = properties.getPath(Settings.KV_STORAGE_PATH);
        return of(dbType, path);
    }

    public static @NotNull KeyValueSettings of(@NotNull DbType type, @NotNull Path path) {
        return new KeyValueSettings(type, path);
    }

    public @NotNull KeyValueSettings with(@NotNull DbType type) {
        return of(type, path);
    }

    public @NotNull KeyValueSettings with(@NotNull Path path) {
        return of(type, path);
    }

    public boolean isDefaults() {
        return equals(DEFAULTS);
    }
}
