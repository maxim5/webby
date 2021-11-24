package io.webby.examples.model;

import com.google.common.primitives.Ints;
import io.webby.orm.api.Connector;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import io.webby.testing.MaliciousTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class StringModelTableTest extends SqliteTableTest<String, StringModel, StringModelTable>
        implements PrimaryKeyTableTest<String, StringModel, StringModelTable>,
                   MaliciousTableTest<String, StringModel, StringModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE string_model (
                id TEXT PRIMARY KEY,
                sequence TEXT,
                chars TEXT,
                raw_bytes BLOB
            )
        """);
        table = new StringModelTable(connector);
    }

    @Override
    public @NotNull String[] keys() {
        return array("foo", "bar");
    }

    @Override
    public @NotNull String[] maliciousKeys() {
        return SqlInjection.MALICIOUS_STRING_INPUTS;
    }

    @Override
    public @NotNull StringModel createEntity(@NotNull String key, int version) {
        return new StringModel(key, key, key.toCharArray(), Ints.toByteArray(version));
    }
}
