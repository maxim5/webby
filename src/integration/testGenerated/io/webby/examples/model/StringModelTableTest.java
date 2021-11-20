package io.webby.examples.model;

import com.google.common.primitives.Ints;
import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class StringModelTableTest extends BaseModelKeyTableTest<String, StringModel, StringModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE string_model (
                id TEXT PRIMARY KEY,
                sequence TEXT,
                chars TEXT,
                raw_bytes BLOB
            )
        """);
        keys = array("foo", "bar");
        maliciousKeys = SqlInjection.MALICIOUS_STRING_INPUTS;
        table = new StringModelTable(connection);
    }

    @Override
    protected @NotNull StringModel createEntity(@NotNull String key, int version) {
        return new StringModel(key, key, key.toCharArray(), Ints.toByteArray(version));
    }
}
