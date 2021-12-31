package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.MaliciousTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class NullableModelTableTest
        extends SqlDbTableTest<String, NullableModel, NullableModelTable>
        implements PrimaryKeyTableTest<String, NullableModel, NullableModelTable>,
                   MaliciousTableTest<String, NullableModel, NullableModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new NullableModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull String[] keys() {
        return array("foo", "");
    }

    @Override
    public @NotNull String[] maliciousKeys() {
        return SqlInjections.MALICIOUS_STRING_INPUTS;
    }

    @Override
    public @NotNull NullableModel createEntity(@NotNull String key, int version) {
        // TODO: nullable nested
        return new NullableModel(key, null, null, new NullableModel.Nested(version, null));
    }
}
