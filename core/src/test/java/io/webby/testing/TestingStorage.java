package io.webby.testing;

import io.webby.db.kv.KeyValueSettings;
import io.webby.db.kv.StorageType;

public class TestingStorage {
    public static final KeyValueSettings KEY_VALUE_DEFAULT = KeyValueSettings.of(StorageType.JAVA_MAP);
}
