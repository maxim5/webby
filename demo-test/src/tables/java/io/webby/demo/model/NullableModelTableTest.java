package io.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.webby.testing.MaliciousTableTest;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import static io.spbx.util.testing.TestingBasics.array;

public class NullableModelTableTest
        extends SqlDbTableTest<NullableModel, NullableModelTable>
        implements PrimaryKeyTableTest<String, NullableModel, NullableModelTable>,
                   MaliciousTableTest<String, NullableModel, NullableModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new NullableModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
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
        return new NullableModel(key, null, null, '\0', version == 0 ? null : new NullableModel.Nested(version, null));
    }
}
