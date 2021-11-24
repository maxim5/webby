package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import io.webby.testing.MaliciousTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class NullableModelTableTest
        extends SqliteTableTest<String, NullableModel, NullableModelTable>
        implements PrimaryKeyTableTest<String, NullableModel, NullableModelTable>,
                   MaliciousTableTest<String, NullableModel, NullableModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE nullable_model (
                id TEXT PRIMARY KEY,
                str TEXT,
                timestamp INTEGER,
                nested_id INTEGER,
                nested_s TEXT
            )
        """);
        table = new NullableModelTable(connector);
    }

    @Override
    public @NotNull String[] keys() {
        return array("foo", "");
    }

    @Override
    public @NotNull String[] maliciousKeys() {
        return SqlInjection.MALICIOUS_STRING_INPUTS;
    }

    @Override
    public @NotNull NullableModel createEntity(@NotNull String key, int version) {
        // TODO: nullable nested
        return new NullableModel(key, null, null, new NullableModel.Nested(version, null));
    }
}
