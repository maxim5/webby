package io.webby.app;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.db.kv.StorageType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@CanIgnoreReturnValue
public class StorageSettings {
    private boolean keyValueStorageEnabled = true;
    private Path storagePath = Path.of("storage");
    private StorageType storageType = StorageType.JAVA_MAP;

    private boolean sqlStorageEnabled = false;

    public boolean isKeyValueStorageEnabled() {
        return keyValueStorageEnabled;
    }

    public @NotNull StorageSettings disableKeyValueStorage() {
        this.keyValueStorageEnabled = false;
        return this;
    }

    public @NotNull StorageSettings enableKeyValueStorage(@NotNull StorageType storageType) {
        this.keyValueStorageEnabled = true;
        return setKeyValueStorageType(storageType);
    }

    public @NotNull Path storagePath() {
        return storagePath;
    }

    public @NotNull StorageSettings setKeyValueStoragePath(@NotNull Path storagePath) {
        this.storagePath = storagePath;
        return this;
    }

    public @NotNull StorageSettings setKeyValueStoragePath(@NotNull String storagePath) {
        this.storagePath = Path.of(storagePath);
        return this;
    }

    public @NotNull StorageType storageType() {
        return storageType;
    }

    public @NotNull StorageSettings setKeyValueStorageType(@NotNull StorageType storageType) {
        this.storageType = storageType;
        return this;
    }

    public boolean isSqlStorageEnabled() {
        return sqlStorageEnabled;
    }

    public @NotNull StorageSettings disableSqlStorage() {
        sqlStorageEnabled = false;
        return this;
    }

    public @NotNull StorageSettings enableSqlStorage() {
        sqlStorageEnabled = true;
        return this;
    }
}
