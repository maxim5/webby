package io.webby.testing;

import io.webby.db.kv.KeyValueSettings;
import io.webby.db.kv.DbType;

public class TestingStorage {
    public static final KeyValueSettings KEY_VALUE_DEFAULT = KeyValueSettings.of(DbType.JAVA_MAP);
}
