package io.spbx.webby.app;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.Immutable;
import io.spbx.util.props.PropertyMap;
import io.spbx.webby.db.kv.KeyValueSettings;
import io.spbx.webby.db.sql.SqlSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

@Immutable
@CheckReturnValue
public record StorageSettings(@Nullable KeyValueSettings keyValueSettings, @Nullable SqlSettings sqlSettings) {
    public static @NotNull StorageSettings of(@Nullable KeyValueSettings keyValueSettings,
                                              @Nullable SqlSettings sqlSettings) {
        return new StorageSettings(keyValueSettings, sqlSettings);
    }

    public static @NotNull StorageSettings fromProperties(@NotNull PropertyMap properties) {
        return of(KeyValueSettings.fromProperties(properties),
                  SqlSettings.fromProperties(properties));
    }

    public boolean isKeyValueEnabled() {
        return keyValueSettings != null;
    }

    public boolean isSqlEnabled() {
        return sqlSettings != null;
    }

    public @NotNull KeyValueSettings keyValueSettingsOrDie() {
        return requireNonNull(keyValueSettings, "Key-value storage not enabled: did you call `settings.enableKeyValue()`?");
    }

    public @NotNull SqlSettings sqlSettingsOrDie() {
        return requireNonNull(sqlSettings, "SQL storage not enabled: did you call `settings.enableSql()`?");
    }

    public @NotNull StorageSettings disableKeyValue() {
        return of(null, sqlSettings);
    }

    public @NotNull StorageSettings withKeyValue(@NotNull KeyValueSettings keyValueSettings) {
        return of(keyValueSettings, sqlSettings);
    }

    public @NotNull StorageSettings disableSql() {
        return of(keyValueSettings, null);
    }

    public @NotNull StorageSettings enableSql(@NotNull SqlSettings sqlSettings) {
        return of(keyValueSettings, sqlSettings);
    }
}
