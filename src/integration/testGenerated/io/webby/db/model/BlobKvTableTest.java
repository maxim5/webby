package io.webby.db.model;

import com.google.common.primitives.Ints;
import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class BlobKvTableTest extends BaseModelKeyTableTest<byte[], BlobKv, BlobKvTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE blob_kv (
                id BLOB PRIMARY KEY,
                value BLOB
            )
        """);
        keys = array(new byte[] {1}, new byte[] {2});
        table = new BlobKvTable(connection);
    }

    @Override
    protected @NotNull BlobKv createEntity(byte @NotNull [] key, int version) {
        return new BlobKv(key, version == 0 ? null : Ints.toByteArray(version));
    }
}
