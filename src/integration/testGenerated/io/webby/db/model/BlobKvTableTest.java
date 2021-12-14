package io.webby.db.model;

import com.google.common.primitives.Ints;
import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class BlobKvTableTest
        extends SqlDbTableTest<byte[], BlobKv, BlobKvTable>
        implements PrimaryKeyTableTest<byte[], BlobKv, BlobKvTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new BlobKvTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public byte[] @NotNull [] keys() {
        return array(new byte[] {1}, new byte[] {2});
    }

    @Override
    public @NotNull BlobKv createEntity(byte @NotNull [] key, int version) {
        return new BlobKv(key, version == 0 ? null : Ints.toByteArray(version));
    }
}
