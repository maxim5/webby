package io.webby.app;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.db.kv.KeyValueSettings;
import io.webby.db.sql.SqlSettings;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

@CanIgnoreReturnValue
public class StorageSettings {
    private final Settings settings;

    private KeyValueSettings keyValueSettings = KeyValueSettings.AUTO_DETECT;
    private SqlSettings sqlSettings = null;

    public StorageSettings(@NotNull Settings settings) {
        this.settings = settings;
    }

    public boolean isKeyValueEnabled() {
        return keyValueSettings != null;
    }

    public @NotNull StorageSettings disableKeyValue() {
        keyValueSettings = null;
        return this;
    }

    public @NotNull StorageSettings enableKeyValue(@NotNull KeyValueSettings keyValueSettings) {
        this.keyValueSettings = keyValueSettings;
        return this;
    }

    public @NotNull KeyValueSettings keyValueSettingsOrDie() {
        return requireNonNull(keyValueSettings, "Key-value storage not enabled: did you call `settings.enableKeyValue()`?");
    }

    public boolean isSqlEnabled() {
        return sqlSettings != null;
    }

    public @NotNull StorageSettings disableSql() {
        sqlSettings = null;
        return this;
    }

    public @NotNull StorageSettings enableSql(@NotNull SqlSettings sqlSettings) {
        this.sqlSettings = sqlSettings;
        return this;
    }

    public @NotNull SqlSettings sqlSettingsOrDie() {
        return requireNonNull(sqlSettings, "SQL storage not enabled: did you call `settings.enableSql()`?");
    }
}
