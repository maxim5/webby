package io.webby.db.model;

import com.google.common.primitives.Ints;
import io.webby.orm.api.Connector;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static io.spbx.util.testing.TestingBasics.array;
import static io.spbx.util.testing.TestingBytes.assertBytes;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class BlobKvTableTest
        extends SqlDbTableTest<BlobKv, BlobKvTable>
        implements PrimaryKeyTableTest<byte[], BlobKv, BlobKvTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new BlobKvTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public byte[] @NotNull [] keys() {
        return array(new byte[] {1}, new byte[] {2});
    }

    @Override
    public @NotNull BlobKv createEntity(byte @NotNull [] key, int version) {
        return new BlobKv(key, version == 0 ? null : Ints.toByteArray(version));
    }

    @Override
    @Test
    public void keyOf() {
        assumeKeys(1);
        byte[] key = keys()[0];
        BlobKv entity = createEntity(key);
        assertBytes(table().keyOf(entity)).isEqualTo(key);
    }

    @Override
    @Test
    public void getBatchByPk() {
        assumeTrue(false, "Byte arrays are not hashable, hence can't be used for map keys");
    }
}
