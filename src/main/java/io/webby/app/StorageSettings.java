package io.webby.app;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.db.kv.StorageType;
import io.webby.db.sql.SqlSettings;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

@CanIgnoreReturnValue
public class StorageSettings {
    private final Settings settings;

    private boolean keyValueStorageEnabled = true;
    private Path storagePath = Path.of("storage");
    private StorageType keyValueStorageType = null;

    private SqlSettings sqlSettings = null;

    public StorageSettings(@NotNull Settings settings) {
        this.settings = settings;
    }

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

    public @NotNull Path keyValueStoragePath() {
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

    public boolean isKeyValueStorageSet() {
        return keyValueStorageType != null;
    }

    public @NotNull StorageType keyValueStorageTypeOrDefault() {
        return keyValueStorageType != null ? keyValueStorageType : StorageType.JAVA_MAP;
    }

    public @NotNull StorageSettings setKeyValueStorageType(@NotNull StorageType storageType) {
        this.keyValueStorageType = storageType;
        return this;
    }

    public boolean isSqlStorageEnabled() {
        return sqlSettings != null;
    }

    public @NotNull StorageSettings disableSqlStorage() {
        sqlSettings = null;
        return this;
    }

    public @NotNull StorageSettings enableSqlStorage(@NotNull SqlSettings sqlSettings) {
        this.sqlSettings = sqlSettings;
        return this;
    }

    public @NotNull SqlSettings sqlSettingsOrDie() {
        return requireNonNull(sqlSettings, "SQL storage not enabled: did you call `settings.enableSqlStorage()`?");
    }
}
