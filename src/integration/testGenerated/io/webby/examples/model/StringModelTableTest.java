package io.webby.examples.model;

import com.google.common.primitives.Ints;
import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.MaliciousTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class StringModelTableTest extends SqlDbTableTest<String, StringModel, StringModelTable>
        implements PrimaryKeyTableTest<String, StringModel, StringModelTable>,
                   MaliciousTableTest<String, StringModel, StringModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new StringModelTable(connector);
        connector().runner().runMultiUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull String[] keys() {
        return array("foo", "bar", "baz");
    }

    @Override
    public @NotNull String[] maliciousKeys() {
        return SqlInjections.MALICIOUS_STRING_INPUTS;
    }

    @Override
    public @NotNull StringModel createEntity(@NotNull String key, int version) {
        return new StringModel(key, key, key.toCharArray(), Ints.toByteArray(version));
    }
}
