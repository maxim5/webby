package io.spbx.webby.testing;

import io.spbx.webby.db.kv.DbType;
import io.spbx.webby.db.kv.KeyValueSettings;

public class TestingStorage {
    public static final KeyValueSettings KEY_VALUE_DEFAULT = KeyValueSettings.of(DbType.JAVA_MAP);
}
